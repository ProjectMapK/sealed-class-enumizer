package org.wrongwrong.gradle

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// IC 回帰マトリクス後半（改名・構造変更）: 設計00 §5.3 #6（末端・外側クラスの改名）・#9（中間 sealed の
// 増減）と、§5.3 表に無い基底自身の改名（docs/テストケース管理.md TC-IC-016/017/024/055）。
// #5（配置変更で不変）との対比 — 宣言の改名は entries の並び・label を変える — が本クラスの重点
class IcRegressionRenameTest {
    // #6a 末端自身の改名（TC-IC-016）: ClassId 変化で entries の FQN 順が再配置され、label も変わる。
    // 併せて TC-IC-008（Si.kt 帰属の EntriesHolder が共連れ再生成され新順序を反映 = P1 の実証）を観測する
    @Test
    fun case6aLeafRenameReordersEntriesAndChangesLabel() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic6a-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val siGenerated0 = IcTestSupport.classDigests(dir).filterKeys(IcBasicFixture::isSiGenerated)

        TestKitHarness.replaceInFile(dir, IcBasicFixture.FOO_FILE, "class Foo(val v: Int) : SI", "class Aoo(val v: Int) : SI")
        TestKitHarness.replaceInFile(dir, IcBasicFixture.USE_FILE, "Foo.Companion -> \"foo\"", "Aoo.Companion -> \"foo\"")
        TestKitHarness.replaceInFile(dir, IcBasicFixture.MAIN_FILE, "describe(Foo(1))", "describe(Aoo(1))")
        val renamed = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))

        assertEquals(
            listOf(
                "ENTRIES=Aoo,Bar,Leaf",
                "TI_ENTRIES=T1",
                "DESCRIBE=foo,bar,leaf",
                "PROBE_FOO=IAE:No enumish entry with label 'Foo' in SI",
                "PROBE_LEAF=Leaf",
                "TRY_BAZ=IAE:No enumish entry with label 'Baz' in SI",
                "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SI",
            ),
            renamed,
        )
        // Si.kt 帰属の生成物（EntriesHolder を含む）が再生成され、並びの変化がバイトにも現れる
        val siGenerated1 = IcTestSupport.classDigests(dir).filterKeys(IcBasicFixture::isSiGenerated)
        assertTrue(siGenerated0 != siGenerated1, "並びの再配置が Si.kt 帰属の生成物へ反映されること")
    }

    // #6b 外側クラスの改名（TC-IC-017）: label は不変（末端の単純名依存）で、
    // 並びと enumizedClass の指す ClassId だけが変わる。#6a との差分の対比
    @Test
    fun case6bOuterClassRenameReordersEntriesButKeepsLabel() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic6b-")
        assertEquals(IcBasicFixture.BASELINE_OUT, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))

        TestKitHarness.replaceInFile(dir, IcBasicFixture.OUTER_FILE, "class Outer {", "class AOuter {")
        TestKitHarness.replaceInFile(dir, IcBasicFixture.USE_FILE, "Outer.Leaf -> \"leaf\"", "AOuter.Leaf -> \"leaf\"")
        TestKitHarness.replaceInFile(dir, IcBasicFixture.MAIN_FILE, "describe(Outer.Leaf)", "describe(AOuter.Leaf)")
        val renamed = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))

        assertEquals(
            listOf(
                "ENTRIES=Leaf,Bar,Foo",
                "TI_ENTRIES=T1",
                "DESCRIBE=foo,bar,leaf",
                "PROBE_FOO=Foo",
                "PROBE_LEAF=Leaf",
                "TRY_BAZ=IAE:No enumish entry with label 'Baz' in SI",
                "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SI",
            ),
            renamed,
        )
    }

    // 基底自身の改名（TC-IC-055。§5.3 #6 は末端・外側の改名で、基底改名は別扱い）:
    // 生成物の全 ClassId が変わるが、末端の ClassId は不変のため entries の内容・相対順は不変。
    // valueOf の失敗メッセージ（enumizedRootClass.simpleName 由来）だけが SX へ変わる
    @Test
    fun case55BaseRenameKeepsEntriesAndChangesFailureMessage() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic55-")
        assertEquals(IcBasicFixture.BASELINE_OUT, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))

        listOf(
            IcBasicFixture.SI_FILE, IcBasicFixture.FOO_FILE, IcBasicFixture.BAR_FILE,
            IcBasicFixture.OUTER_FILE, IcBasicFixture.USE_FILE, IcBasicFixture.MAIN_FILE,
        ).forEach { TestKitHarness.replaceInFile(dir, it, "SI", "SX") }
        val renamed = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))

        assertEquals(
            listOf(
                "ENTRIES=Bar,Foo,Leaf",
                "TI_ENTRIES=T1",
                "DESCRIBE=foo,bar,leaf",
                "PROBE_FOO=Foo",
                "PROBE_LEAF=Leaf",
                "TRY_BAZ=IAE:No enumish entry with label 'Baz' in SX",
                "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SX",
            ),
            renamed,
        )
    }

    // #9 中間 sealed の追加・除去（TC-IC-024）: kind 集合は不変のため kind-when は成立し続けるが、
    // 末端がトップレベルのまま中間へ付け替わると entries の並びが入れ子展開順へ変わる（設計00 §6.2）。
    // 除去で並びが復帰し、生成物も clean とバイト一致する
    @Test
    fun case9IntermediateSealedInsertionAndRemoval() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic9-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val siGenerated0 = IcTestSupport.classDigests(dir).filterKeys(IcBasicFixture::isSiGenerated)

        TestKitHarness.writeFile(
            dir, IcBasicFixture.MID_FILE,
            "package org.wrongwrong.icfix\n\n// 編集ケース #9 で挿入される中間 sealed（docs/テストケース管理.md TC-IC-024）\nsealed interface Mid : SI\n",
        )
        TestKitHarness.replaceInFile(dir, IcBasicFixture.BAR_FILE, "data object Bar : SI", "data object Bar : Mid")
        val inserted = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(
            listOf(
                "ENTRIES=Foo,Bar,Leaf",
                "TI_ENTRIES=T1",
                "DESCRIBE=foo,bar,leaf",
                "PROBE_FOO=Foo",
                "PROBE_LEAF=Leaf",
                "TRY_BAZ=IAE:No enumish entry with label 'Baz' in SI",
                "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SI",
            ),
            inserted,
        )

        TestKitHarness.deleteFile(dir, IcBasicFixture.MID_FILE)
        TestKitHarness.replaceInFile(dir, IcBasicFixture.BAR_FILE, "data object Bar : Mid", "data object Bar : SI")
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(siGenerated0, IcTestSupport.classDigests(dir).filterKeys(IcBasicFixture::isSiGenerated))
    }

    // #8 label 衝突の導入・解消（TC-IC-023）: 別クラスへネストした同名末端の追加（既存ファイルへの
    // 追記）で ENUMIZE_LABEL_CLASH が発火し、逆編集で解消される（診断の単調性）。
    // 解消後は clean ビルドとバイト一致まで復帰する
    @Test
    fun case8LabelClashIntroductionAndResolution() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic8-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)

        // Outer 内へ単純名 Foo の末端を追記 → トップレベル末端 Foo と label 衝突
        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.OUTER_FILE,
            "    data object Leaf : SI",
            "    data object Leaf : SI\n\n    data object Foo : SI",
        )
        val clash = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue(
            "Duplicated label" in clash.output && "'Foo'" in clash.output,
            "ENUMIZE_LABEL_CLASH の発火:\n${clash.output}",
        )

        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.OUTER_FILE,
            "    data object Leaf : SI\n\n    data object Foo : SI",
            "    data object Leaf : SI",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // 多段中間 sealed チェーン（TC-IC-060。2 段の TC-IC-025 相当も同形）: チェーン中間ファイルの
    // ABI 非変更編集は基底ファイルを共連れしない（中間 sealed は生成物から参照されず依存辺が無い）が、
    // 末端側のボディ充填は origin 駆動で走るため成立し、再帰収集の結果は不変である。
    // 階層が変わらないラウンドであり、全生成物が編集前とバイト一致する
    @Test
    fun case60MidChainEditRecollectsRecursively() {
        val dir = IcTestSupport.prepare("ic-chain", "ic60-")
        val expected = listOf("ENTRIES=Leaf", "KIND=Leaf")
        assertEquals(expected, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val digests0 = IcTestSupport.classDigests(dir)

        TestKitHarness.replaceInFile(
            dir, "src/main/kotlin/org/wrongwrong/chain/Mid2.kt",
            "// チェーン 2 段目の中間 sealed（TC-IC-060 の編集対象）",
            "// チェーン 2 段目の中間 sealed（TC-IC-060 の編集対象。編集ラウンド）",
        )
        assertEquals(expected, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // 空階層の境界（TC-IC-061）: 全末端の削除で entries=[]・valueOf は常に失敗、のまま生成が成立し、
    // 最初の末端（既存ファイルへの追記）で entries=[First] へ遷移する
    @Test
    fun case61EmptyHierarchyBoundary() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic61-")
        // 生成 API の kind 名指しを持つ参照側をスタブへ差し替え（末端ゼロでも成立する形にする）
        TestKitHarness.writeFile(
            dir, IcBasicFixture.USE_FILE,
            "package org.wrongwrong.icfix\n\n// 一時スタブ（空階層境界の観測用 = TC-IC-061）\nfun describe(value: SI): String = value.toString()\n",
        )
        TestKitHarness.writeFile(
            dir, IcBasicFixture.MAIN_FILE,
            "package org.wrongwrong.icfix\n\n// 一時スタブ（空階層境界の観測用 = TC-IC-061）\nfun main() {\n" +
                "    println(\"OUT:ENTRIES=\" + SI.Enumish.entries.joinToString(\",\") { it.label })\n" +
                "    println(\"OUT:NOLABEL=\" + try {\n" +
                "        SI.Enumish.valueOf(\"NoSuch\").label\n" +
                "    } catch (e: IllegalArgumentException) {\n" +
                "        \"IAE:\" + e.message\n" +
                "    })\n}\n",
        )
        assertEquals(
            listOf("ENTRIES=Bar,Foo,Leaf", "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SI"),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )

        // 全末端の削除 → entries が空になり、EntriesHolder は空リストで成立する
        TestKitHarness.deleteFile(dir, IcBasicFixture.FOO_FILE)
        TestKitHarness.deleteFile(dir, IcBasicFixture.BAR_FILE)
        TestKitHarness.deleteFile(dir, IcBasicFixture.OUTER_FILE)
        assertEquals(
            listOf("ENTRIES=", "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SI"),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )

        // 最初の末端の追加（基底ファイルへの追記であり、基底が同一ラウンドへ入る形）
        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.SI_FILE,
            "sealed interface SI",
            "sealed interface SI {\n    data object First : SI\n}",
        )
        assertEquals(
            listOf("ENTRIES=First", "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SI"),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )
    }
}
