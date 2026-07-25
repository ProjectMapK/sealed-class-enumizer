package org.wrongwrong.gradle

import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

// 基底不在ラウンドのシナリオ（docs/コンパイラプラグイン設計00.md §5.4・§9-4、docs/テストケース管理.md TC-IC-039/040・TC-ORD-050）:
// 多ファイル sealed 階層 × プラグイン生成コード × IC。基底ファイルが IC ラウンドへ入らない編集
// （新規ファイルでの末端追加）でも、IR 側のボディ充填は origin 駆動で走るため成立する。合否は
// 「クラッシュしないこと」ではなく、最終成果物（実行時 entries・生成 .class のバイト）と診断
// （kind-when の網羅性・LABEL_CLASH）が clean ビルドと一致することで判定する。
// もう一方の基底不在ラウンド（中間 sealed ファイルの単独編集）は TC-IC-060 が持つ
class IcBaseAbsentRoundTest {
    private val siFile = "src/main/kotlin/org/wrongwrong/baseabsent/Si.kt"
    private val leafAFile = "src/main/kotlin/org/wrongwrong/baseabsent/LeafA.kt"
    private val leafBFile = "src/main/kotlin/org/wrongwrong/baseabsent/LeafB.kt"
    private val leafDFile = "src/main/kotlin/org/wrongwrong/baseabsent/LeafD.kt"
    private val useFile = "src/main/kotlin/org/wrongwrong/baseabsent/Use.kt"
    private val generatedPrefix = "org/wrongwrong/baseabsent/SI\$"
    private val expectedOut = listOf("ENTRIES=LeafA,LeafB,LeafC", "DESCRIBE=a,b,c")
    private val expectedOutWithD = listOf("ENTRIES=LeafA,LeafB,LeafC,LeafD", "DESCRIBE=a,b,c")
    private val newLeafSource =
        "package org.wrongwrong.baseabsent\n\n" +
            "// 新規ファイルで追加される末端（基底不在ラウンドの引き金）\ndata object LeafD : SI\n"

    // 既存 kind を宣言するファイルの連続編集（R1〜R4）では基底ファイルが同一ラウンドへ共連れされる。
    // 新規ファイルでの末端追加（R5）は共連れが起きず基底不在ラウンドになるが、成果物は clean と一致する
    @Test
    fun consecutiveEditsAndNewLeafFileMatchCleanBuild() {
        val dir = IcTestSupport.prepare("ic-base-absent", "icabs-")
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

    // 基底不在ラウンドでも階層系の診断が出ること: 新規ファイルの末端は利用側の kind-when を
    // 非網羅にし、同一ラウンドで追加した同名末端どうしは LABEL_CLASH になる
    @Test
    fun baseAbsentRoundReportsHierarchyDiagnostics() {
        val dir = IcTestSupport.prepare("ic-base-absent", "icabsdiag-")
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
            "src/main/kotlin/org/wrongwrong/baseabsent/Dup1.kt",
            "package org.wrongwrong.baseabsent\n\nobject H1 {\n    data object Dup : SI\n}\n",
        )
        TestKitHarness.writeFile(
            dir,
            "src/main/kotlin/org/wrongwrong/baseabsent/Dup2.kt",
            "package org.wrongwrong.baseabsent\n\nobject H2 {\n    data object Dup : SI\n}\n",
        )
        val clash = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue(
            "Duplicated label" in clash.output,
            "同一ラウンドで追加した同名末端どうしが LABEL_CLASH になること:\n${clash.output}",
        )
    }

    // 各ラウンド: ビルドが成功し（診断の偽陽性なし = TC-IC-059）、entries が stale にならず、
    // Si.kt 帰属の生成物（EntriesHolder を含む）が clean 基準とバイト一致する（TC-ORD-050）
    private fun assertRoundIsGreen(dir: Path, generated0: Map<String, String>) {
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(generated0, generatedDigests(dir))
    }

    private fun generatedDigests(dir: Path): Map<String, String> =
        IcTestSupport.classDigests(dir).filterKeys { it.startsWith(generatedPrefix) }
}
