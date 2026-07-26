package org.wrongwrong.gradle

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

// IC 回帰マトリクス（docs/test/ケース06-ビルド動態.md BLD-01・BLD-07〜14・BLD-19/20/22/23/26/27）。
// ic-basic フィクスチャ（多ファイル分散 SI + 独立階層 TI + ネスト基底 NB + 無関係ファイル）で
// 「ビルド → 編集 → 再ビルド」を駆動し、実行時 OUT: 行・生成 .class のバイト・
// 出力タイムスタンプ（= 再コンパイル追跡）を clean 基準値と比較する
class IcRegressionTest {
    private val bazSource =
        "package org.wrongwrong.icfix\n\n// 編集ケース #2 で追加される末端（docs/test/ケース06-ビルド動態.md BLD-08）\ndata object Baz : SI\n"

    // BASELINE_OUT の一部の行だけを差し替えた期待値を組む（行の並びは固定）
    private fun expectedOut(vararg overrides: Pair<String, String>): List<String> =
        IcBasicFixture.BASELINE_OUT.map { line ->
            overrides.firstOrNull { line.startsWith(it.first) }?.second ?: line
        }

    // docs/test/ケース06-ビルド動態.md BLD-01: clean 基準の採取と $EntriesHolder の基底ファイル帰属
    // （トップレベル基底 SI・ネスト基底 NB の両形。基底ファイルのみの編集で当該階層の EntriesHolder
    // だけが書き直されることを帰属の観測とする）
    @Test
    fun cleanBaselineCapturesOutputsAndEntriesHolderPlacement() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "icbl-")
        assertEquals(
            IcBasicFixture.BASELINE_OUT,
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )
        val keys = IcTestSupport.classDigests(dir).keys
        assertTrue(
            keys.any { IcBasicFixture.isSiGenerated(it) && "EntriesHolder" in it },
            "SI の \$EntriesHolder クラスが存在すること: $keys",
        )
        assertTrue(
            keys.any { IcBasicFixture.isNbGenerated(it) && "EntriesHolder" in it },
            "NB の \$EntriesHolder クラスが存在すること: $keys",
        )

        // SI 基底ファイルのみの編集 → SI の EntriesHolder は書き直され NB 側は不変
        val times0 = IcTestSupport.classTimes(dir)
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.SI_FILE,
            "// IC 回帰フィクスチャの基底。",
            "// IC 回帰フィクスチャの基底（帰属観測）。",
        )
        TestKitHarness.build(dir, "compileKotlin")
        val changedSi = IcTestSupport.changedKeys(times0, IcTestSupport.classTimes(dir))
        assertTrue(
            changedSi.any { IcBasicFixture.isSiGenerated(it) && "EntriesHolder" in it },
            "SI の EntriesHolder が基底編集で書き直されること: $changedSi",
        )
        assertTrue(changedSi.none(IcBasicFixture::isNbGenerated), "NB 側は不変であること: $changedSi")

        // NB 基底ファイルのみの編集 → NB の EntriesHolder は書き直され SI 側は不変
        val times1 = IcTestSupport.classTimes(dir)
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.NB_FILE,
            "// 非 sealed 外側クラス内のネスト基底",
            "// 非 sealed 外側クラス内のネスト基底（帰属観測）",
        )
        TestKitHarness.build(dir, "compileKotlin")
        val changedNb = IcTestSupport.changedKeys(times1, IcTestSupport.classTimes(dir))
        assertTrue(
            changedNb.any { IcBasicFixture.isNbGenerated(it) && "EntriesHolder" in it },
            "NB の EntriesHolder が基底編集で書き直されること: $changedNb",
        )
        assertTrue(changedNb.none(IcBasicFixture::isSiGenerated), "SI 側は不変であること: $changedNb")
    }

    // docs/test/ケース06-ビルド動態.md BLD-07: #1 末端編集で階層全体が共連れ再生成され、
    // OUT 不変・独立階層（TI / NB）と無関係ファイルは非 dirty・編集対象以外はバイト一致
    @Test
    fun case1LeafEditRegeneratesHierarchyWithIdenticalBytes() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic1-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)
        val times0 = IcTestSupport.classTimes(dir)

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.FOO_FILE,
            "class Foo(val v: Int) : SI",
            "class Foo(val v: Int) : SI {\n    // 編集ケース #1: ABI に影響しない private メンバーの追記\n    private fun note(): Int = v\n}",
        )
        val second = TestKitHarness.build(dir, "runMain")

        assertEquals(baseline, IcTestSupport.outLines(second))
        val digests1 = IcTestSupport.classDigests(dir)
        val changed = IcTestSupport.changedKeys(times0, IcTestSupport.classTimes(dir))
        assertTrue(changed.any(IcBasicFixture::isSiGenerated), "SI 生成物が再生成されること: $changed")
        assertTrue(
            "${IcBasicFixture.CLASS_PREFIX}/Bar.class" in changed,
            "Bar.class が共連れ再生成されること: $changed",
        )
        assertTrue(changed.none(IcBasicFixture::isTiOutput), "TI 出力は不変であること: $changed")
        assertTrue(changed.none(IcBasicFixture::isNbGenerated), "NB 生成物は不変であること: $changed")
        assertTrue(
            "${IcBasicFixture.CLASS_PREFIX}/UnrelatedKt.class" !in changed,
            "無関係ファイルは非共連れ: $changed",
        )
        val fooKey = "${IcBasicFixture.CLASS_PREFIX}/Foo.class"
        assertEquals(digests0.filterKeys { it != fooKey }, digests1.filterKeys { it != fooKey })
    }

    // docs/test/ケース06-ビルド動態.md BLD-08: #2 新規ファイルの末端追加は 1 巡目が基底不在でも
    // else 無し kind-when の非網羅エラーで検出される
    @Test
    fun case2LeafAdditionIsDetectedByWhenExhaustiveness() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic2-")
        assertEquals(
            IcBasicFixture.BASELINE_OUT,
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )

        TestKitHarness.writeFile(dir, IcBasicFixture.BAZ_FILE, bazSource)
        val addFailure = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue(
            "exhaustive" in addFailure.output,
            "末端追加で kind-when が非網羅になること:\n${addFailure.output}",
        )
    }

    // docs/test/ケース06-ビルド動態.md BLD-09: #3 末端削除は削除 kind の名指しエラーとして検出され、
    // stale 出力が掃除される。復帰で clean バイト一致（#2 の意味論 = entries 反映を経由して観測する）
    @Test
    fun case3LeafRemovalSweepsStaleOutputsAndRestores() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic3-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)

        TestKitHarness.writeFile(dir, IcBasicFixture.BAZ_FILE, bazSource)
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.USE_FILE,
            "    Bar -> \"bar\"",
            "    Bar -> \"bar\"\n    Baz -> \"baz\"",
        )
        assertEquals(
            expectedOut("ENTRIES=" to "ENTRIES=Bar,Baz,Foo,Leaf", "TRY_BAZ=" to "TRY_BAZ=Baz"),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )

        // 末端ファイルの削除 → 削除 kind を名指しする利用側がエラーになる
        TestKitHarness.deleteFile(dir, IcBasicFixture.BAZ_FILE)
        val removeFailure = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue(
            "Baz" in removeFailure.output,
            "削除された kind の名指しがエラーになること:\n${removeFailure.output}",
        )

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.USE_FILE,
            "    Bar -> \"bar\"\n    Baz -> \"baz\"",
            "    Bar -> \"bar\"",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val digestsFinal = IcTestSupport.classDigests(dir)
        assertTrue(
            "${IcBasicFixture.CLASS_PREFIX}/Baz.class" !in digestsFinal.keys,
            "Baz.class の stale 掃除",
        )
        assertEquals(digests0, digestsFinal)
    }

    // docs/test/ケース06-ビルド動態.md BLD-10: #4 基底のみ編集で末端側も共連れ再生成されるが
    // 全出力バイト一致・独立階層は非 dirty
    @Test
    fun case4BaseOnlyEditKeepsLeafOutputBytes() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic4-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)
        val times0 = IcTestSupport.classTimes(dir)

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.SI_FILE,
            "// IC 回帰フィクスチャの基底。",
            "// IC 回帰フィクスチャの基底（#4 基底のみ編集）。",
        )
        val second = TestKitHarness.build(dir, "runMain")

        assertEquals(baseline, IcTestSupport.outLines(second))
        val changed = IcTestSupport.changedKeys(times0, IcTestSupport.classTimes(dir))
        assertTrue(
            "${IcBasicFixture.CLASS_PREFIX}/Bar.class" in changed,
            "末端出力が共連れ再生成されること: $changed",
        )
        assertTrue(changed.none(IcBasicFixture::isTiOutput), "TI 出力は不変であること: $changed")
        assertTrue(changed.none(IcBasicFixture::isNbGenerated), "NB 生成物は不変であること: $changed")
        assertTrue(
            "${IcBasicFixture.CLASS_PREFIX}/UnrelatedKt.class" !in changed,
            "無関係ファイルは非共連れ: $changed",
        )
        // コメントのみの編集（行数不変）のため全出力がバイト一致する
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // docs/test/ケース06-ビルド動態.md BLD-11: #5 ファイル改名 + 宣言のファイル間移動で
    // OUT・生成物とも不変（FQN 順正規化 = 物理配置非依存）
    @Test
    fun case5FileRenameAndDeclarationMoveKeepBytes() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic5-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)
        val renamedFile = "src/main/kotlin/org/wrongwrong/icfix/Renamed.kt"

        // #5a: Foo.kt を内容そのまま Renamed.kt へ改名
        IcTestSupport.moveFile(dir, IcBasicFixture.FOO_FILE, renamedFile)
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val digests1 = IcTestSupport.classDigests(dir)
        val fooPrefix = "${IcBasicFixture.CLASS_PREFIX}/Foo"
        assertEquals(
            digests0.filterKeys { !it.startsWith(fooPrefix) },
            digests1.filterKeys { !it.startsWith(fooPrefix) },
        )

        // #5b: Bar の宣言を Bar.kt から Renamed.kt へ移動（宣言内容は不変）
        val renamedContent = IcTestSupport.readFile(dir, renamedFile)
        TestKitHarness.deleteFile(dir, IcBasicFixture.BAR_FILE)
        TestKitHarness.writeFile(
            dir,
            renamedFile,
            renamedContent + "\n// 編集ケース #5b: ファイル間移動してきた末端（宣言内容は不変）\ndata object Bar : SI\n",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val digests2 = IcTestSupport.classDigests(dir)
        val movedPrefixes = listOf(fooPrefix, "${IcBasicFixture.CLASS_PREFIX}/Bar")
        assertEquals(
            digests0.filterKeys { key -> movedPrefixes.none(key::startsWith) },
            digests2.filterKeys { key -> movedPrefixes.none(key::startsWith) },
        )
        assertEquals(
            digests0.filterKeys(IcBasicFixture::isSiGenerated),
            digests2.filterKeys(IcBasicFixture::isSiGenerated),
        )
    }

    // docs/test/ケース06-ビルド動態.md BLD-12: #6a 末端改名で entries 再配置 + label 変化・
    // valueOf(旧名) 失敗・基底側生成物のバイト変化（P1 実証）
    @Test
    fun case6aLeafRenameReordersEntriesAndChangesLabel() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic6a-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val siGenerated0 = IcTestSupport.classDigests(dir).filterKeys(IcBasicFixture::isSiGenerated)

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.FOO_FILE,
            "class Foo(val v: Int) : SI",
            "class Aoo(val v: Int) : SI",
        )
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.USE_FILE,
            "Foo.Companion -> \"foo\"",
            "Aoo.Companion -> \"foo\"",
        )
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.MAIN_FILE,
            "describe(Foo(1))",
            "describe(Aoo(1))",
        )
        val renamed = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))

        assertEquals(
            expectedOut(
                "ENTRIES=" to "ENTRIES=Aoo,Bar,Leaf",
                "PROBE_FOO=" to "PROBE_FOO=IAE:No enumish entry with label 'Foo' in SI",
            ),
            renamed,
        )
        val siGenerated1 = IcTestSupport.classDigests(dir).filterKeys(IcBasicFixture::isSiGenerated)
        assertTrue(siGenerated0 != siGenerated1, "並びの再配置が Si.kt 帰属の生成物へ反映されること")
    }

    // docs/test/ケース06-ビルド動態.md BLD-13: #6b 外側クラス改名は並びと enumizedClass のみ変化・
    // label / valueOf 不変
    @Test
    fun case6bOuterRenameReordersEntriesKeepsLabel() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic6b-")
        assertEquals(
            IcBasicFixture.BASELINE_OUT,
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.OUTER_FILE,
            "class Outer {",
            "class AOuter {",
        )
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.USE_FILE,
            "Outer.Leaf -> \"leaf\"",
            "AOuter.Leaf -> \"leaf\"",
        )
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.MAIN_FILE,
            "describe(Outer.Leaf)",
            "describe(AOuter.Leaf)",
        )
        assertEquals(
            expectedOut("ENTRIES=" to "ENTRIES=Leaf,Bar,Foo"),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )
    }

    // docs/test/ケース06-ビルド動態.md BLD-14: 基底改名（全 ClassId 変化）で entries 不変・
    // valueOf 失敗メッセージの基底名のみ変化
    @Test
    fun baseRenameKeepsEntriesAndChangesFailureMessage() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic14-")
        assertEquals(
            IcBasicFixture.BASELINE_OUT,
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )

        listOf(
                IcBasicFixture.SI_FILE,
                IcBasicFixture.FOO_FILE,
                IcBasicFixture.BAR_FILE,
                IcBasicFixture.OUTER_FILE,
                IcBasicFixture.USE_FILE,
                IcBasicFixture.MAIN_FILE,
            )
            .forEach { TestKitHarness.replaceInFile(dir, it, "SI", "SX") }
        assertEquals(
            expectedOut(
                "TRY_BAZ=" to "TRY_BAZ=IAE:No enumish entry with label 'Baz' in SX",
                "NOLABEL=" to "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SX",
            ),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )
    }

    // docs/test/ケース06-ビルド動態.md BLD-19: #8 label 衝突の 2 形（編集導入 + 別ファイル追加）で
    // LABEL_CLASH が発火し、解消で clean バイト一致まで復帰する
    @Test
    fun case8LabelClashIntroductionAndResolution() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic8-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)

        // 形 1: 既存ファイルへの編集導入（Outer 内へ単純名 Foo の末端を追記）
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.OUTER_FILE,
            "    data object Leaf : SI",
            "    data object Leaf : SI\n\n    data object Foo : SI",
        )
        val clash = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue(
            "Duplicated label" in clash.output && "'Foo'" in clash.output,
            "編集導入による ENUMIZE_LABEL_CLASH の発火:\n${clash.output}",
        )
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.OUTER_FILE,
            "    data object Leaf : SI\n\n    data object Foo : SI",
            "    data object Leaf : SI",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))

        // 形 2: 新規ファイル追加（報告先 = 当該ラウンドの当事者）
        TestKitHarness.writeFile(
            dir,
            IcBasicFixture.DUP_FILE,
            "package org.wrongwrong.icfix\n\n// #8 ファイル追加形の同名末端（docs/test/ケース06-ビルド動態.md BLD-19）\nclass DupHost {\n    data object Foo : SI\n}\n",
        )
        val clash2 = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue(
            "Duplicated label" in clash2.output && "'Foo'" in clash2.output,
            "ファイル追加による ENUMIZE_LABEL_CLASH の発火:\n${clash2.output}",
        )
        TestKitHarness.deleteFile(dir, IcBasicFixture.DUP_FILE)
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // docs/test/ケース06-ビルド動態.md BLD-20: #9 中間 sealed 挿入 + 末端付け替えで入れ子展開順へ
    // 並び再計算・除去で復帰バイト一致
    @Test
    fun case9IntermediateSealedInsertionAndRemoval() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic9-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val siGenerated0 = IcTestSupport.classDigests(dir).filterKeys(IcBasicFixture::isSiGenerated)

        TestKitHarness.writeFile(
            dir,
            IcBasicFixture.MID_FILE,
            "package org.wrongwrong.icfix\n\n// 編集ケース #9 で挿入される中間 sealed（docs/test/ケース06-ビルド動態.md BLD-20）\nsealed interface Mid : SI\n",
        )
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.BAR_FILE,
            "data object Bar : SI",
            "data object Bar : Mid",
        )
        assertEquals(
            expectedOut("ENTRIES=" to "ENTRIES=Foo,Bar,Leaf"),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )

        TestKitHarness.deleteFile(dir, IcBasicFixture.MID_FILE)
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.BAR_FILE,
            "data object Bar : Mid",
            "data object Bar : SI",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(
            siGenerated0,
            IcTestSupport.classDigests(dir).filterKeys(IcBasicFixture::isSiGenerated),
        )
    }

    // docs/test/ケース06-ビルド動態.md BLD-22: #10 無関係ファイルの編集は階層（SI / TI / NB）を
    // dirty にしない（P3 の dirty 税なし = V7）
    @Test
    fun case10UnrelatedEditDoesNotDirtyHierarchies() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic10-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val times0 = IcTestSupport.classTimes(dir)

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.UNRELATED_FILE,
            "fun unrelatedHelper(): Int = 41",
            "fun unrelatedHelper(): Int = 42",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val changed = IcTestSupport.changedKeys(times0, IcTestSupport.classTimes(dir))
        assertTrue(
            "${IcBasicFixture.CLASS_PREFIX}/UnrelatedKt.class" in changed,
            "編集ファイルの出力は書かれる: $changed",
        )
        assertTrue(changed.none(IcBasicFixture::isHierarchyOutput), "階層出力は再生成されないこと: $changed")
        assertTrue(changed.none(IcBasicFixture::isTiOutput), "TI 出力は再生成されないこと: $changed")
        assertTrue(changed.none(IcBasicFixture::isNbGenerated), "NB 生成物は再生成されないこと: $changed")
    }

    // docs/test/ケース06-ビルド動態.md BLD-23: @Enumize 除去で stale 掃除・再付与 + 参照復元で
    // clean バイト一致
    @Test
    fun enumizeToggleSweepsStaleAndRestoresBytes() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic23t-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)
        val originalUse = IcTestSupport.readFile(dir, IcBasicFixture.USE_FILE)
        val originalMain = IcTestSupport.readFile(dir, IcBasicFixture.MAIN_FILE)

        // 生成 API を参照しないスタブへ差し替え（除去ラウンドを成功ビルドとして観測するため）
        TestKitHarness.writeFile(
            dir,
            IcBasicFixture.USE_FILE,
            "package org.wrongwrong.icfix\n\n// 一時スタブ（@Enumize 除去ラウンド用: 生成 API を参照しない）\nfun describe(value: SI): String = value.toString()\n",
        )
        TestKitHarness.writeFile(
            dir,
            IcBasicFixture.MAIN_FILE,
            "package org.wrongwrong.icfix\n\n// 一時スタブ（@Enumize 除去ラウンド用）\nfun main() {\n    println(\"OUT:TI_ENTRIES=\" + TI.Enumish.entries.joinToString(\",\") { it.label })\n}\n",
        )
        TestKitHarness.build(dir, "compileKotlin")
        assertTrue(
            IcTestSupport.classDigests(dir).keys.any(IcBasicFixture::isSiGenerated),
            "除去前は生成物が存在",
        )

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.SI_FILE,
            "@Enumize\nsealed interface SI",
            "sealed interface SI",
        )
        TestKitHarness.build(dir, "compileKotlin")
        val sweptKeys = IcTestSupport.classDigests(dir).keys
        assertTrue(
            sweptKeys.none(IcBasicFixture::isSiGenerated),
            "SI\$Enumish* の stale 掃除: $sweptKeys",
        )
        assertTrue(
            "${IcBasicFixture.CLASS_PREFIX}/Foo\$Companion.class" !in sweptKeys,
            "自動生成 companion の掃除",
        )

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.SI_FILE,
            "sealed interface SI",
            "@Enumize\nsealed interface SI",
        )
        TestKitHarness.writeFile(dir, IcBasicFixture.USE_FILE, originalUse)
        TestKitHarness.writeFile(dir, IcBasicFixture.MAIN_FILE, originalMain)
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // docs/test/ケース06-ビルド動態.md BLD-26: 末端をトップレベル → 基底本体内へ移設すると
    // entries の並びだけが変わり label / valueOf は不変
    @Test
    fun leafMoveIntoBaseBodyReordersEntriesKeepsLabel() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic26-")
        assertEquals(
            IcBasicFixture.BASELINE_OUT,
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )

        TestKitHarness.deleteFile(dir, IcBasicFixture.BAR_FILE)
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.SI_FILE,
            "sealed interface SI",
            "sealed interface SI {\n    data object Bar : SI\n}",
        )
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.MAIN_FILE,
            "describe(Bar)",
            "describe(SI.Bar)",
        )
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.USE_FILE,
            "    Bar -> \"bar\"",
            "    SI.Bar -> \"bar\"",
        )
        // FQN が p.Bar → p.SI.Bar へ変わり並びが再配置（Foo < Outer.Leaf < SI.Bar）
        assertEquals(
            expectedOut("ENTRIES=" to "ENTRIES=Foo,Leaf,Bar"),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )
    }

    // docs/test/ケース06-ビルド動態.md BLD-27: 基底可視性の変更（公開面追随込み）で
    // OUT・順序・末端側生成物バイトとも不変
    @Test
    fun baseVisibilityEditKeepsOrderAndOutputs() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic27-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.SI_FILE,
            "sealed interface SI",
            "internal sealed interface SI",
        )
        // 基底 internal 化に伴う公開面の追随（public 関数のシグネチャに internal 型を出さない）
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.USE_FILE,
            "fun describe(value: SI)",
            "internal fun describe(value: SI)",
        )
        val second = TestKitHarness.build(dir, "runMain")

        assertEquals(baseline, IcTestSupport.outLines(second))
        val digests1 = IcTestSupport.classDigests(dir)
        val leafKeys = { key: String ->
            key.startsWith("${IcBasicFixture.CLASS_PREFIX}/Foo") ||
                key.startsWith("${IcBasicFixture.CLASS_PREFIX}/Bar") ||
                key.startsWith("${IcBasicFixture.CLASS_PREFIX}/Outer")
        }
        assertEquals(digests0.filterKeys(leafKeys), digests1.filterKeys(leafKeys))
    }
}
