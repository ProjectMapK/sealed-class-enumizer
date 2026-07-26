package org.wrongwrong.gradle

import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

// ラウンド境界と粒度（docs/test/ケース06-ビルド動態.md BLD-06・BLD-21・BLD-28〜31・BLD-45）。
// ic-rounds（フラット末端 + 多段中間チェーン + 空⇔非空遷移）と ic-kind-accessor
// （参照不能 kind 3 態様の IR-only アクセサ）と ic-scope-tracking
// （候補判定のスコープ解決を変える跨ファイル編集）を担う
class IcRoundsTest {
    private val siFile = "src/main/kotlin/org/wrongwrong/icrounds/Si.kt"
    private val leafAFile = "src/main/kotlin/org/wrongwrong/icrounds/LeafA.kt"
    private val leafBFile = "src/main/kotlin/org/wrongwrong/icrounds/LeafB.kt"
    private val leafCFile = "src/main/kotlin/org/wrongwrong/icrounds/LeafC.kt"
    private val leafDFile = "src/main/kotlin/org/wrongwrong/icrounds/LeafD.kt"
    private val mid2File = "src/main/kotlin/org/wrongwrong/icrounds/Mid2.kt"
    private val useFile = "src/main/kotlin/org/wrongwrong/icrounds/Use.kt"
    private val mainFile = "src/main/kotlin/org/wrongwrong/icrounds/Main.kt"
    private val generatedPrefix = "org/wrongwrong/icrounds/SI\$"
    private val expectedOut =
        listOf("ENTRIES=LeafA,LeafB,LeafC", "DESCRIBE=a,b,c", "CH_ENTRIES=ChLeaf", "CH_KIND=ChLeaf")
    private val expectedOutWithD =
        listOf(
            "ENTRIES=LeafA,LeafB,LeafC,LeafD",
            "DESCRIBE=a,b,c",
            "CH_ENTRIES=ChLeaf",
            "CH_KIND=ChLeaf",
        )
    private val newLeafSource =
        "package org.wrongwrong.icrounds\n\n" +
            "// 新規ファイルで追加される末端（基底不在ラウンドの引き金）\ndata object LeafD : SI\n"

    private val accessorFixture = "ic-kind-accessor"
    private val accessorHiddenFile = "src/main/kotlin/org/wrongwrong/ickacc/KaHidden.kt"
    private val accessorGeneratedPrefix = "org/wrongwrong/ickacc/KaSi\$Enumish"
    private val accessorOut = listOf("ENTRIES=Leaf,KaPriv,Visible", "VALUEOF=Leaf,KaPriv")

    private val scopeFixture = "ic-scope-tracking"
    private val scAliasFile = "src/main/kotlin/org/wrongwrong/icscope/ScAlias.kt"
    private val scAliasMovedFile = "src/main/kotlin/org/wrongwrong/icscope/ScAliasMoved.kt"
    private val scNameFile = "src/main/kotlin/org/wrongwrong/icscope/ScName.kt"
    private val scopeOut = listOf("ENTRIES=ScA,ScAliasLeaf", "DESCRIBE=a")
    private val scopeOutDetached = listOf("ENTRIES=ScA", "DESCRIBE=a")

    // docs/test/ケース06-ビルド動態.md BLD-28: 連続編集（R1〜R4）+ 新規末端ファイル追加
    // （基底不在ラウンド）で各ラウンド green・成果物が clean 一致・削除で復帰
    @Test
    fun consecutiveEditsAndNewLeafFileMatchCleanBuild() {
        val dir = IcTestSupport.prepare("ic-rounds", "icrnd-")
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val generated0 = generatedDigests(dir)

        // R1: 末端 class のコメントのみ編集（ABI 不変）
        TestKitHarness.replaceInFile(
            dir,
            leafAFile,
            "// 末端 class（companion はプラグイン自動生成）。連続編集ラウンドの主対象",
            "// 末端 class（companion はプラグイン自動生成）。連続編集ラウンドの主対象（R1 編集）",
        )
        assertRoundIsGreen(dir, generated0)

        // R2: 同じ末端へ private メンバーを追加（ABI 不変の宣言追加）
        TestKitHarness.replaceInFile(
            dir,
            leafAFile,
            "class LeafA(val v: Int) : SI",
            "class LeafA(val v: Int) : SI {\n    // R2: private メンバー追加\n    private fun r2(): Int = v\n}",
        )
        assertRoundIsGreen(dir, generated0)

        // R3: 別の末端ファイルを編集（連続ラウンドの編集対象を切り替える）
        TestKitHarness.replaceInFile(dir, leafBFile, "// 末端 object", "// 末端 object（R3 編集）")
        assertRoundIsGreen(dir, generated0)

        // R4: 基底ファイルを編集
        TestKitHarness.replaceInFile(
            dir,
            siFile,
            "// 多ファイル sealed 階層の基底（連続編集ラウンドで毎回共連れ再コンパイルされる）",
            "// 多ファイル sealed 階層の基底（連続編集ラウンドで毎回共連れ再コンパイルされる。R4 編集）",
        )
        assertRoundIsGreen(dir, generated0)

        // R5: 末端の「新規ファイル」追加 = 基底不在ラウンド。entries へ反映され、
        // 全生成物が同一ソースの clean ビルドとバイト一致する（合否条件そのもの）
        TestKitHarness.writeFile(dir, leafDFile, newLeafSource)
        TestKitHarness.replaceInFile(
            dir,
            useFile,
            "    LeafB -> \"b\"",
            "    LeafB -> \"b\"\n    LeafD -> \"d\"",
        )
        assertEquals(expectedOutWithD, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val incremental = IcTestSupport.classDigests(dir)
        assertEquals(
            expectedOutWithD,
            IcTestSupport.outLines(TestKitHarness.build(dir, "clean", "runMain")),
        )
        assertEquals(IcTestSupport.classDigests(dir), incremental, "基底不在ラウンドの成果物が clean と一致すること")

        // R6: 末端ファイルの削除も IC 直行で成立し、基準状態の生成物とバイト一致まで復帰する
        TestKitHarness.deleteFile(dir, leafDFile)
        TestKitHarness.replaceInFile(
            dir,
            useFile,
            "    LeafB -> \"b\"\n    LeafD -> \"d\"",
            "    LeafB -> \"b\"",
        )
        assertRoundIsGreen(dir, generated0)
    }

    // docs/test/ケース06-ビルド動態.md BLD-29: 基底不在ラウンドでも kind-when 非網羅と
    // 同一ラウンド追加の同名 2 末端の LABEL_CLASH が発火する（部分集合ビューで偽陽性なし）
    @Test
    fun baseAbsentRoundReportsMonotonicDiagnostics() {
        val dir = IcTestSupport.prepare("ic-rounds", "icrndg-")
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))

        TestKitHarness.writeFile(dir, leafDFile, newLeafSource)
        val exhaustiveness = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue(
            "exhaustive" in exhaustiveness.output,
            "新規ファイルの末端追加で kind-when が非網羅になること:\n${exhaustiveness.output}",
        )
        TestKitHarness.deleteFile(dir, leafDFile)

        // 同一ラウンドで追加した 2 つの新規末端が同じ label を持つ構成
        TestKitHarness.writeFile(
            dir,
            "src/main/kotlin/org/wrongwrong/icrounds/Dup1.kt",
            "package org.wrongwrong.icrounds\n\nobject H1 {\n    data object Dup : SI\n}\n",
        )
        TestKitHarness.writeFile(
            dir,
            "src/main/kotlin/org/wrongwrong/icrounds/Dup2.kt",
            "package org.wrongwrong.icrounds\n\nobject H2 {\n    data object Dup : SI\n}\n",
        )
        val clash = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue(
            "Duplicated label" in clash.output,
            "同一ラウンドで追加した同名末端どうしが LABEL_CLASH になること:\n${clash.output}",
        )
    }

    // docs/test/ケース06-ビルド動態.md BLD-30: 全末端削除 → entries 空（valueOf 全失敗）→
    // 最初の末端追加で反映（空⇔非空遷移）
    @Test
    fun emptyHierarchyBoundaryTransition() {
        val dir = IcTestSupport.prepare("ic-rounds", "icrnde-")
        // 生成 API の kind 名指しを持つ参照側をスタブへ差し替え（末端ゼロでも成立する形にする）
        TestKitHarness.writeFile(
            dir,
            useFile,
            "package org.wrongwrong.icrounds\n\n// 一時スタブ（空階層境界の観測用）\nfun describe(value: SI): String = value.toString()\n",
        )
        TestKitHarness.writeFile(
            dir,
            mainFile,
            "package org.wrongwrong.icrounds\n\n// 一時スタブ（空階層境界の観測用）\nfun main() {\n" +
                "    println(\"OUT:ENTRIES=\" + SI.Enumish.entries.joinToString(\",\") { it.label })\n" +
                "    println(\"OUT:NOLABEL=\" + try {\n" +
                "        SI.Enumish.valueOf(\"NoSuch\").label\n" +
                "    } catch (e: IllegalArgumentException) {\n" +
                "        \"IAE:\" + e.message\n" +
                "    })\n}\n",
        )
        assertEquals(
            listOf(
                "ENTRIES=LeafA,LeafB,LeafC",
                "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SI",
            ),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )

        // 全末端の削除 → entries が空になり、EntriesHolder は空リストで成立する
        TestKitHarness.deleteFile(dir, leafAFile)
        TestKitHarness.deleteFile(dir, leafBFile)
        TestKitHarness.deleteFile(dir, leafCFile)
        assertEquals(
            listOf("ENTRIES=", "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SI"),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )

        // 最初の末端の追加（基底ファイルへの追記 = 基底が同一ラウンドへ入る形）
        TestKitHarness.replaceInFile(
            dir,
            siFile,
            "sealed interface SI",
            "sealed interface SI {\n    data object First : SI\n}",
        )
        assertEquals(
            listOf("ENTRIES=First", "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SI"),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )
    }

    // docs/test/ケース06-ビルド動態.md BLD-21: 多段中間チェーンの非参照中間のみ編集
    // （基底不在ラウンド）で OUT 不変・全生成物バイト一致
    @Test
    fun midOnlyEditKeepsOutputsWithoutBaseRound() {
        val dir = IcTestSupport.prepare("ic-rounds", "icrndm-")
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val digests0 = IcTestSupport.classDigests(dir)

        TestKitHarness.replaceInFile(
            dir,
            mid2File,
            "// チェーン 2 段目の中間 sealed（docs/test/ケース06-ビルド動態.md BLD-21 の編集対象）",
            "// チェーン 2 段目の中間 sealed（docs/test/ケース06-ビルド動態.md BLD-21 の編集対象。編集ラウンド）",
        )
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // docs/test/ケース06-ビルド動態.md BLD-31: 参照不能 kind 3 態様で末端編集の共連れ安定 +
    // 新規末端の基底不在ラウンド収束（IR-only アクセサの再構成）
    @Test
    fun accessorEntriesSurviveEditsAndNewLeafRound() {
        val dir = IcTestSupport.prepare(accessorFixture, "ickacc-")
        assertEquals(accessorOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val generated0 =
            IcTestSupport.classDigests(dir).filterKeys { it.startsWith(accessorGeneratedPrefix) }

        // 末端ファイル単独の ABI 非変更編集 → 共連れでも基底帰属の生成物（createEntries）は安定
        TestKitHarness.replaceInFile(
            dir,
            accessorHiddenFile,
            "IR-only アクセサ経由で load",
            "IR-only アクセサ経由で取得",
        )
        assertEquals(accessorOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(
            generated0,
            IcTestSupport.classDigests(dir).filterKeys { it.startsWith(accessorGeneratedPrefix) },
        )

        // 新規末端ファイルの追加（基底不在ラウンド）→ 2 巡目収束で entries へ反映・clean 一致
        TestKitHarness.writeFile(
            dir,
            "src/main/kotlin/org/wrongwrong/ickacc/KaNew.kt",
            "package org.wrongwrong.ickacc\n\n// 基底不在ラウンドの引き金となる新規末端\ndata object KaNew : KaSi\n",
        )
        val added = listOf("ENTRIES=Leaf,KaNew,KaPriv,Visible", "VALUEOF=Leaf,KaPriv")
        assertEquals(added, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val incremental = IcTestSupport.classDigests(dir)
        assertEquals(added, IcTestSupport.outLines(TestKitHarness.build(dir, "clean", "runMain")))
        assertEquals(IcTestSupport.classDigests(dir), incremental, "アクセサを含む生成物が clean と一致すること")
    }

    // docs/test/ケース06-ビルド動態.md BLD-45: 候補判定のスコープ解決を変える跨ファイル編集
    // （typealias の付け替え・宣言ファイル張り替え・同一 pkg 囮の削除）で clean / incremental の
    // 生成物一致と失敗パリティを実測する（docs/コンパイラプラグイン設計00.md §10 V7 の帰結直接観測）
    @Test
    fun scopeResolutionEditsInOtherFilesMatchCleanBuild() {
        val dir = IcTestSupport.prepare(scopeFixture, "icscp-")
        assertEquals(scopeOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val digests0 = IcTestSupport.classDigests(dir)

        // R2: エイリアスの展開先を非階層 interface へ付け替え（末端ファイル自体は不変）→
        // ScAliasLeaf が階層を離脱し、同編集を加えた clean ビルドと全生成物バイト一致
        TestKitHarness.replaceInFile(
            dir,
            scAliasFile,
            "typealias ScAlias = ScBase",
            "typealias ScAlias = ScOther",
        )
        assertEquals(scopeOutDetached, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val detached = IcTestSupport.classDigests(dir)
        assertTrue(
            "org/wrongwrong/icscope/ScAliasLeaf\$Companion.class" !in detached.keys,
            "階層離脱で自動生成 companion が stale 掃除されること: ${detached.keys}",
        )
        val cleanDir = IcTestSupport.prepare(scopeFixture, "icscpc-")
        TestKitHarness.replaceInFile(
            cleanDir,
            scAliasFile,
            "typealias ScAlias = ScBase",
            "typealias ScAlias = ScOther",
        )
        assertEquals(
            scopeOutDetached,
            IcTestSupport.outLines(TestKitHarness.build(cleanDir, "runMain")),
        )
        assertEquals(IcTestSupport.classDigests(cleanDir), detached, "付け替え後の生成物が clean と一致すること")

        // R3: 復元 → R1 基準と全生成物バイト一致
        TestKitHarness.replaceInFile(
            dir,
            scAliasFile,
            "typealias ScAlias = ScOther",
            "typealias ScAlias = ScBase",
        )
        assertEquals(scopeOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))

        // R4: エイリアス宣言ファイルの張り替え（同内容の別ファイルへ移設）→ ENTRIES 不変・
        // ファイル帰属の差分（エイリアスのファサードクラスと kotlin_module）を除き R1 と一致
        IcTestSupport.moveFile(dir, scAliasFile, scAliasMovedFile)
        assertEquals(scopeOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val moved = IcTestSupport.classDigests(dir)
        assertEquals(
            digests0.filterKeys { !isAliasFileAttribution(it) },
            moved.filterKeys { !isAliasFileAttribution(it) },
        )
        IcTestSupport.moveFile(dir, scAliasMovedFile, scAliasFile)
        assertEquals(scopeOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))

        // R5: 同一 pkg 囮の削除 → incremental / clean の buildAndFail が
        // 同じ未解決エラー（ScShadowLeaf.kt の行）になる失敗パリティ → 復元
        val scNameSource = IcTestSupport.readFile(dir, scNameFile)
        TestKitHarness.deleteFile(dir, scNameFile)
        val incrementalErrors = shadowLeafErrors(TestKitHarness.buildAndFail(dir, "compileKotlin"))
        val failDir = IcTestSupport.prepare(scopeFixture, "icscpf-")
        TestKitHarness.deleteFile(failDir, scNameFile)
        val cleanErrors = shadowLeafErrors(TestKitHarness.buildAndFail(failDir, "compileKotlin"))
        assertTrue(incrementalErrors.isNotEmpty(), "囮の削除で ScShadowLeaf.kt の未解決エラーが出ること")
        assertEquals(cleanErrors, incrementalErrors)

        TestKitHarness.writeFile(dir, scNameFile, scNameSource)
        assertEquals(scopeOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // docs/test/ケース06-ビルド動態.md BLD-06: アクセサ込み生成物の clean / UP-TO-DATE / from-cache
    // バイト一致（IR-only 生成の決定性）
    @Test
    fun accessorOutputsAreDeterministic() {
        val dir = IcTestSupport.prepare(accessorFixture, "ickaccd-")
        val first = TestKitHarness.build(dir, "runMain")
        assertEquals(accessorOut, IcTestSupport.outLines(first))
        val digests0 = IcTestSupport.classDigests(dir)

        val second = TestKitHarness.build(dir, "runMain")
        assertEquals(TaskOutcome.UP_TO_DATE, second.task(":compileKotlin")?.outcome)
        assertEquals(accessorOut, IcTestSupport.outLines(second))
        assertEquals(digests0, IcTestSupport.classDigests(dir))

        TestKitHarness.build(dir, "clean")
        val third = TestKitHarness.build(dir, "runMain")
        assertEquals(TaskOutcome.FROM_CACHE, third.task(":compileKotlin")?.outcome)
        assertEquals(accessorOut, IcTestSupport.outLines(third))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // 各ラウンド: ビルドが成功し（診断の偽陽性なし）、entries が stale にならず、
    // Si.kt 帰属の生成物（EntriesHolder を含む）が clean 基準とバイト一致する
    private fun assertRoundIsGreen(dir: Path, generated0: Map<String, String>) {
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(generated0, generatedDigests(dir))
    }

    private fun generatedDigests(dir: Path): Map<String, String> =
        IcTestSupport.classDigests(dir).filterKeys { it.startsWith(generatedPrefix) }

    // BLD-45 R4 のファイル帰属差分: エイリアス宣言を持つファイルのファサードクラスと、
    // ファサード名を列挙する kotlin_module だけが移設で変わる
    private fun isAliasFileAttribution(key: String): Boolean =
        key == "org/wrongwrong/icscope/ScAliasKt.class" ||
            key == "org/wrongwrong/icscope/ScAliasMovedKt.class" ||
            key.startsWith("META-INF/")

    // BLD-45 R5 の失敗パリティ用: ScShadowLeaf.kt に帰属するエラー行を
    // 絶対パス差を除いた形（ファイル名 + 行位置 + 本文）で抽出する
    private fun shadowLeafErrors(result: BuildResult): List<String> =
        result.output
            .lineSequence()
            .filter { "ScShadowLeaf.kt" in it }
            .map { it.substring(it.indexOf("ScShadowLeaf.kt")).trim() }
            .distinct()
            .toList()
}
