package org.wrongwrong.gradle

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// IC 回帰マトリクス前半: 設計00 §5.3 の編集ケース #1〜#5 と @Enumize の付与・除去
// （docs/テストケース管理.md C 軸 TC-IC-009〜015・029〜031・038・041・043）。
// 各ケースは「ビルド → 編集 → 再ビルド」で、実行時 OUT: 行・生成 .class のバイト・
// 出力タイムスタンプ（= 再コンパイル追跡）を clean 基準値と比較する
class IcRegressionTest {
    // #1 末端側のみ編集（TC-IC-009）: 階層全体が共連れ再コンパイルされ、entries は不変。
    // 併せて TC-IC-010/031（共連れ後の Si.kt 出力バイト一致）・TC-IC-041（独立階層 TI が非 dirty）・
    // TC-IC-043（無関係ファイルが非共連れ）を同一編集で観測する
    @Test
    fun case1LeafSideEditKeepsEntriesAndDoesNotDirtyOthers() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic1-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)
        val times0 = IcTestSupport.classTimes(dir)

        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.FOO_FILE,
            "class Foo(val v: Int) : SI",
            "class Foo(val v: Int) : SI {\n    // 編集ケース #1: ABI に影響しない private メンバーの追記\n    private fun note(): Int = v\n}",
        )
        val second = TestKitHarness.build(dir, "runMain")

        assertEquals(baseline, IcTestSupport.outLines(second))
        val digests1 = IcTestSupport.classDigests(dir)
        val changed = IcTestSupport.changedKeys(times0, IcTestSupport.classTimes(dir))
        // 階層不変条件: 編集していない Si.kt / Bar.kt の出力も書き直される（共連れ）
        assertTrue(changed.any(IcBasicFixture::isSiGenerated), "SI 生成物が再生成されること: $changed")
        assertTrue("${IcBasicFixture.CLASS_PREFIX}/Bar.class" in changed, "Bar.class が共連れ再生成されること: $changed")
        // 逆方向: 独立階層 TI と無関係ファイルは dirty にならない（P3・V7）
        assertTrue(changed.none(IcBasicFixture::isTiOutput), "TI 出力は不変であること: $changed")
        assertTrue("${IcBasicFixture.CLASS_PREFIX}/UnrelatedKt.class" !in changed, "無関係ファイルは非共連れ: $changed")
        // 決定性: 編集した Foo.class 以外はバイト一致（TC-IC-010/031）
        val fooKey = "${IcBasicFixture.CLASS_PREFIX}/Foo.class"
        assertEquals(digests0.filterKeys { it != fooKey }, digests1.filterKeys { it != fooKey })
    }

    // #2 末端の追加を IC 直行で行う仕様通りの形（TC-IC-011）: 期待は「階層共連れ + 利用側の
    // kind-when が網羅性エラー」。実測は初回 IC ラウンドが新規ファイル単独で走り、基底がラウンド外の
    // ため FIR 生成宣言（asEnumish）に IR ボディが充填されず、バックエンド ICE
    // 「Function has no body」で failed する（KT-86121 型。再現ゲートは Kt86121Test が保持）
    @Disabled("NG: 末端の新規ファイル追加の IC 初回ラウンドがバックエンド ICE（Function has no body / KT-86121 型・TC-IC-011）— docs/修正方針案.md 反映待ち")
    @Test
    fun case2AddLeafIncrementallyDetectedByWhenExhaustiveness() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic2-")
        assertEquals(IcBasicFixture.BASELINE_OUT, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))

        TestKitHarness.writeFile(
            dir, IcBasicFixture.BAZ_FILE,
            "package org.wrongwrong.icfix\n\n// 編集ケース #2 で追加される末端（docs/テストケース管理.md TC-IC-011）\ndata object Baz : SI\n",
        )
        val addFailure = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue("exhaustive" in addFailure.output, "末端追加で kind-when が非網羅になること:\n${addFailure.output}")
    }

    // #2 の意味論（網羅性エラーによる検出・entries への反映）と #3 末端の削除（TC-IC-012）。
    // 追加ラウンドのみ IC 直行が ICE になるため（上の @Disabled = TC-IC-011 の NG）、追加は clean を
    // 挟んで到達し、削除側は仕様どおり IC 直行で検証する。復帰後は clean 基準とバイト一致する
    @Test
    fun case2SemanticsAndCase3RemoveLeafIncrementally() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic23-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)

        // #2 の意味論: 追加された末端は利用側の else 無し kind-when の網羅性エラーで検出される
        // （clean 経由 = フルビルドの検査。IC 直行の検証は @Disabled の TC-IC-011 が持つ）
        TestKitHarness.writeFile(
            dir, IcBasicFixture.BAZ_FILE,
            "package org.wrongwrong.icfix\n\n// 編集ケース #2 で追加される末端（docs/テストケース管理.md TC-IC-011）\ndata object Baz : SI\n",
        )
        TestKitHarness.build(dir, "clean")
        val addFailure = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue("exhaustive" in addFailure.output, "末端追加で kind-when が非網羅になること:\n${addFailure.output}")

        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.USE_FILE,
            "    Bar -> \"bar\"",
            "    Bar -> \"bar\"\n    Baz -> \"baz\"",
        )
        val afterAdd = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(
            listOf(
                "ENTRIES=Bar,Baz,Foo,Leaf",
                "TI_ENTRIES=T1",
                "DESCRIBE=foo,bar,leaf",
                "PROBE_FOO=Foo",
                "PROBE_LEAF=Leaf",
                "TRY_BAZ=Baz",
                "NOLABEL=IAE:No enumish entry with label 'NoSuch' in SI",
            ),
            afterAdd,
        )

        // #3: 末端ファイルの削除は IC 直行で成立し、削除 kind を名指しする利用側がエラーになる
        TestKitHarness.deleteFile(dir, IcBasicFixture.BAZ_FILE)
        val removeFailure = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue("Baz" in removeFailure.output, "削除された kind の名指しがエラーになること:\n${removeFailure.output}")

        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.USE_FILE,
            "    Bar -> \"bar\"\n    Baz -> \"baz\"",
            "    Bar -> \"bar\"",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val digestsFinal = IcTestSupport.classDigests(dir)
        // stale 出力（Baz.class）が IC で掃除され、全生成物が clean ビルドとバイト一致する
        assertTrue("${IcBasicFixture.CLASS_PREFIX}/Baz.class" !in digestsFinal.keys, "Baz.class の stale 掃除")
        assertEquals(digests0, digestsFinal)
    }

    // #4 基底のみ編集（TC-IC-013）: 末端側の生成物も共連れ再生成されるが、
    // 実行時内容・バイトとも不変（TC-IC-038 の末端側出力バイト一致）
    @Test
    fun case4BaseOnlyEditRegeneratesLeafOutputsUnchanged() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic4-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)
        val times0 = IcTestSupport.classTimes(dir)

        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.SI_FILE,
            "// IC 回帰フィクスチャの基底。",
            "// IC 回帰フィクスチャの基底（#4 基底のみ編集）。",
        )
        val second = TestKitHarness.build(dir, "runMain")

        assertEquals(baseline, IcTestSupport.outLines(second))
        val changed = IcTestSupport.changedKeys(times0, IcTestSupport.classTimes(dir))
        assertTrue("${IcBasicFixture.CLASS_PREFIX}/Bar.class" in changed, "末端出力が共連れ再生成されること: $changed")
        assertTrue(changed.none(IcBasicFixture::isTiOutput), "TI 出力は不変であること: $changed")
        assertTrue("${IcBasicFixture.CLASS_PREFIX}/UnrelatedKt.class" !in changed, "無関係ファイルは非共連れ: $changed")
        // コメントのみの編集（行数不変）のため全出力がバイト一致する
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // #5a 内容不変のファイル改名（TC-IC-014）と #5b 末端宣言のファイル間移動（TC-IC-015）:
    // ソースの物理配置に生成物が依存しないこと。生成物（SI$Enumish* = EntriesHolder・生成 Companion）は
    // バイト一致し、改名・移動したファイル自身のクラスのみ SourceFile 属性差分を許容する
    @Test
    fun case5RenameFileAndMoveDeclarationKeepGeneratedBytes() {
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
            dir, renamedFile,
            renamedContent + "\n// 編集ケース #5b: ファイル間移動してきた末端（宣言内容は不変）\ndata object Bar : SI\n",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val digests2 = IcTestSupport.classDigests(dir)
        val movedPrefixes = listOf(fooPrefix, "${IcBasicFixture.CLASS_PREFIX}/Bar")
        assertEquals(
            digests0.filterKeys { key -> movedPrefixes.none(key::startsWith) },
            digests2.filterKeys { key -> movedPrefixes.none(key::startsWith) },
        )
        // 生成物の中核（SI$Enumish 一式）がバイト不変であることを明示的にも固定する
        assertEquals(
            digests0.filterKeys(IcBasicFixture::isSiGenerated),
            digests2.filterKeys(IcBasicFixture::isSiGenerated),
        )
    }

    // #10 階層外の無関係ファイルのみ編集（TC-IC-026）: 階層（SI / TI とも）は dirty にならず、
    // 生成物は再生成されない（P3 の dirty 税なし = V7）。編集したファイルの出力だけが書き直される
    @Test
    fun case10UnrelatedEditDoesNotDirtyHierarchies() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic10-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val times0 = IcTestSupport.classTimes(dir)

        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.UNRELATED_FILE,
            "fun unrelatedHelper(): Int = 41",
            "fun unrelatedHelper(): Int = 42",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val changed = IcTestSupport.changedKeys(times0, IcTestSupport.classTimes(dir))
        assertTrue("${IcBasicFixture.CLASS_PREFIX}/UnrelatedKt.class" in changed, "編集ファイルの出力は書かれる: $changed")
        assertTrue(changed.none(IcBasicFixture::isHierarchyOutput), "階層出力は再生成されないこと: $changed")
        assertTrue(changed.none(IcBasicFixture::isTiOutput), "TI 出力は再生成されないこと: $changed")
    }

    // @Enumize の除去（TC-IC-030: 生成 API の消失と stale 掃除）と付与（TC-IC-029: 出現と
    // clean ビルドとのバイト一致）。参照側は一時スタブへ差し替えて成功ビルドの中で観測する
    @Test
    fun enumizeRemovalSweepsStaleOutputsAndReAdditionMatchesClean() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic2930-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)
        val originalUse = IcTestSupport.readFile(dir, IcBasicFixture.USE_FILE)
        val originalMain = IcTestSupport.readFile(dir, IcBasicFixture.MAIN_FILE)

        // 生成 API を参照しないスタブへ差し替え（除去ラウンドを成功ビルドとして観測するため）
        TestKitHarness.writeFile(
            dir, IcBasicFixture.USE_FILE,
            "package org.wrongwrong.icfix\n\n// 一時スタブ（@Enumize 除去ラウンド用: 生成 API を参照しない）\nfun describe(value: SI): String = value.toString()\n",
        )
        TestKitHarness.writeFile(
            dir, IcBasicFixture.MAIN_FILE,
            "package org.wrongwrong.icfix\n\n// 一時スタブ（@Enumize 除去ラウンド用）\nfun main() {\n    println(\"OUT:TI_ENTRIES=\" + TI.Enumish.entries.joinToString(\",\") { it.label })\n}\n",
        )
        TestKitHarness.build(dir, "compileKotlin")
        assertTrue(IcTestSupport.classDigests(dir).keys.any(IcBasicFixture::isSiGenerated), "除去前は生成物が存在")

        // @Enumize を除去 → 生成物（SI$Enumish* と自動生成 companion）の stale が掃除される
        TestKitHarness.replaceInFile(dir, IcBasicFixture.SI_FILE, "@Enumize\nsealed interface SI", "sealed interface SI")
        TestKitHarness.build(dir, "compileKotlin")
        val sweptKeys = IcTestSupport.classDigests(dir).keys
        assertTrue(sweptKeys.none(IcBasicFixture::isSiGenerated), "SI\$Enumish* の stale 掃除: $sweptKeys")
        assertTrue("${IcBasicFixture.CLASS_PREFIX}/Foo\$Companion.class" !in sweptKeys, "自動生成 companion の掃除")

        // @Enumize を再付与し、参照側も復元 → 実行時・バイトとも clean ビルドと一致する
        TestKitHarness.replaceInFile(dir, IcBasicFixture.SI_FILE, "sealed interface SI", "@Enumize\nsealed interface SI")
        TestKitHarness.writeFile(dir, IcBasicFixture.USE_FILE, originalUse)
        TestKitHarness.writeFile(dir, IcBasicFixture.MAIN_FILE, originalMain)
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }
}
