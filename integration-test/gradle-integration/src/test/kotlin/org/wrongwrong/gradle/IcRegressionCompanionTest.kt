package org.wrongwrong.gradle

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// IC 回帰マトリクス（companion・手動実装・dirty 税）: 設計00 §5.3 #7/#7-b/#7-c・#12 と P3 の
// dirty 税なし（docs/テストケース管理.md TC-IC-018〜022・028・042、TC-GAP-002）
class IcRegressionCompanionTest {
    private val fooCompanionClass = "${IcBasicFixture.CLASS_PREFIX}/Foo\$Companion.class"
    private val fooFactoryClass = "${IcBasicFixture.CLASS_PREFIX}/Foo\$Factory.class"

    // #7 既定名 companion の追加・削除（TC-IC-018）: 自動生成と流用が切り替わるだけで、
    // companion クラスの存在と名前・利用側から見た形（entries / valueOf / asEnumish）は不変
    @Test
    fun case7ManualDefaultCompanionAddAndRemove() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic7-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)
        assertTrue(fooCompanionClass in digests0.keys, "自動生成 companion のクラスが存在すること")

        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.FOO_FILE,
            "class Foo(val v: Int) : SI",
            "class Foo(val v: Int) : SI {\n    companion object\n}",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertTrue(fooCompanionClass in IcTestSupport.classDigests(dir).keys, "手動 companion でもクラス名は不変")

        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.FOO_FILE,
            "class Foo(val v: Int) : SI {\n    companion object\n}",
            "class Foo(val v: Int) : SI",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // #7-b 名前つき companion への切替（TC-IC-019）: Foo$Companion ⇔ Foo$Factory のクラス名変化が
    // ABI 差分として修飾名で名指しする側にだけ伝播し、label と valueOf は不変。旧クラスの stale は掃除される
    @Test
    fun case7bNamedCompanionSwitchPropagatesOnlyToQualifiedReferences() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic7b-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)

        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.FOO_FILE,
            "class Foo(val v: Int) : SI",
            "class Foo(val v: Int) : SI {\n    companion object Factory\n}",
        )
        val failure = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue("Companion" in failure.output, "修飾名 Foo.Companion の名指しが追従を要すること:\n${failure.output}")

        TestKitHarness.replaceInFile(dir, IcBasicFixture.USE_FILE, "Foo.Companion -> \"foo\"", "Foo.Factory -> \"foo\"")
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val keys = IcTestSupport.classDigests(dir).keys
        assertTrue(fooFactoryClass in keys, "Foo\$Factory.class が生成されること")
        assertTrue(fooCompanionClass !in keys, "旧 Foo\$Companion.class の stale が掃除されること")
    }

    // #7-c companion の可視性変更（TC-IC-020）: public ⇔ internal で asEnumish の返り値型が
    // 規則 1（Foo.Companion）⇔ 規則 2（SI.Enumish）で切り替わり、ABI 差分として利用側へ伝播する。
    // private 化は kind の名指し（利用側 when 枝）を言語が拒否する別事象で、entries 構築（IR-only アクセサ）
    // 側の成立は producer-jvm / mpp-producer の KindAccessorTest が固定する（概要 §8・設計02 §4.3）
    @Test
    fun case7cCompanionVisibilityChangesReturnType() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic7c-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)

        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.FOO_FILE,
            "class Foo(val v: Int) : SI",
            "class Foo(val v: Int) : SI {\n    companion object\n}",
        )
        TestKitHarness.writeFile(
            dir, IcBasicFixture.TYPED_USE_FILE,
            "package org.wrongwrong.icfix\n\n// asEnumish の返り値型（規則 1: 具体型 Foo.Companion）へ静的に依存する観測点（TC-IC-020）\nprivate val typedKind: Foo.Companion = Foo(1).asEnumish()\n",
        )
        TestKitHarness.build(dir, "compileKotlin")

        // internal 化 → 返り値型が SI.Enumish へフォールバック（規則 2）し、具体型依存が壊れる
        TestKitHarness.replaceInFile(dir, IcBasicFixture.FOO_FILE, "    companion object\n", "    internal companion object\n")
        val mismatch = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue("Enumish" in mismatch.output, "返り値型の切替が型不一致として観測されること:\n${mismatch.output}")

        TestKitHarness.writeFile(
            dir, IcBasicFixture.TYPED_USE_FILE,
            "package org.wrongwrong.icfix\n\n// 規則 2 フォールバック後の返り値型（SI.Enumish）に合わせた観測点（TC-IC-020）\nprivate val typedKind: SI.Enumish = Foo(1).asEnumish()\n",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))

        // 逆編集で public 具体型（規則 1）へ戻る
        TestKitHarness.replaceInFile(dir, IcBasicFixture.FOO_FILE, "    internal companion object\n", "    companion object\n")
        TestKitHarness.writeFile(
            dir, IcBasicFixture.TYPED_USE_FILE,
            "package org.wrongwrong.icfix\n\n// asEnumish の返り値型（規則 1: 具体型 Foo.Companion）へ静的に依存する観測点（TC-IC-020）\nprivate val typedKind: Foo.Companion = Foo(1).asEnumish()\n",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
    }

    // #7-c の規則 3（TC-IC-022）: 基底（internal）より広い public 末端で companion を末端未満（internal）へ
    // 変更すると ENUMIZE_KIND_TYPE_NOT_DENOTABLE。逆編集で解除される
    @Test
    fun case7cWiderLeafCompanionNarrowingIsNotDenotable() {
        val dir = IcTestSupport.prepare("ic-wider-leaf", "icwl-")
        val wideFile = "src/main/kotlin/org/wrongwrong/widerfix/Wide.kt"
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(listOf("WENTRIES=Wide", "WKIND=Wide"), baseline)

        TestKitHarness.replaceInFile(dir, wideFile, "    companion object\n", "    internal companion object\n")
        val failure = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue("cannot be denoted" in failure.output, "ENUMIZE_KIND_TYPE_NOT_DENOTABLE の発火:\n${failure.output}")

        TestKitHarness.replaceInFile(dir, wideFile, "    internal companion object\n", "    companion object\n")
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
    }

    // #12 階層外の手動実装の追加・削除（TC-IC-028・TC-GAP-002）: 追加は当該ファイル単体で
    // ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY のコンパイルエラーとして顕在化し（= V1-(e) の TODO は
    // エラー化路線で実装済み）、削除で復帰し生成物は clean とバイト一致する
    @Test
    fun case12OutsideManualImplIsRejectedAndRemovalRestoresBytes() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic12-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)

        TestKitHarness.writeFile(
            dir, IcBasicFixture.ROGUE_FILE,
            "package org.wrongwrong.icfix\n\nimport kotlin.reflect.KClass\n\n" +
                "// 階層外の手動実装（設計00 §5.3 #12 = TC-IC-028。単体ファイルでの発火を観測する）\n" +
                "object Rogue : SI.Enumish {\n" +
                "    override val label: String get() = \"Rogue\"\n\n" +
                "    override val enumizedClass: KClass<out SI> get() = SI::class\n" +
                "}\n",
        )
        val failure = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue(
            "implements the generated Enumish" in failure.output,
            "ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY の発火:\n${failure.output}",
        )

        TestKitHarness.deleteFile(dir, IcBasicFixture.ROGUE_FILE)
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // 階層内手動実装（TC-IC-048・V1-(e)）: 末端 class 自身が生成 Enumish を直接実装する許容形
    // （概要 §8）へ切り替えると、inheritors へ手動実装分（Foo 自身）が加わり kind-when に is Foo 枝が
    // 必要になる一方、entries は kind（Foo.Companion）のみで不変。手動実装ファイルの ABI 非変更編集でも
    // 実行時挙動と Si.kt 帰属生成物のバイトが安定している（階層共連れで inheritors 再計算）
    @Test
    fun case48HierarchyInternalManualImplEditKeepsEntries() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic48-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)
        val originalFoo = IcTestSupport.readFile(dir, IcBasicFixture.FOO_FILE)
        val originalUse = IcTestSupport.readFile(dir, IcBasicFixture.USE_FILE)

        // 末端 Foo を「末端でもあり手動実装でもある」形へ（label 手動宣言は EXTENSION_SHADOWED 警告のみ）
        TestKitHarness.writeFile(
            dir, IcBasicFixture.FOO_FILE,
            "package org.wrongwrong.icfix\n\nimport kotlin.reflect.KClass\n\n" +
                "// 階層内の手動実装を兼ねる末端（TC-IC-048。docs/概要.md §8 の許容形）\n" +
                "class Foo(val v: Int) : SI, SI.Enumish {\n" +
                "    override val label: String get() = \"FooManual\"\n\n" +
                "    override val enumizedClass: KClass<out SI> get() = Foo::class\n" +
                "}\n",
        )
        // 手動実装が inheritors に載るため kind-when へ is Foo 枝が要る（概要 §3・§8）
        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.USE_FILE,
            "    Outer.Leaf -> \"leaf\"",
            "    Outer.Leaf -> \"leaf\"\n    is Foo -> \"fooManual\"",
        )
        val manual = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        // 手動実装値は kind でないため entries / valueOf / describe の実行時結果は不変
        assertEquals(baseline, manual)
        val siGeneratedManual = IcTestSupport.classDigests(dir).filterKeys(IcBasicFixture::isSiGenerated)

        // 手動実装ファイルの ABI 非変更編集 → 階層共連れで inheritors 再計算・結果は安定
        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.FOO_FILE,
            "// 階層内の手動実装を兼ねる末端（TC-IC-048。docs/概要.md §8 の許容形）",
            "// 階層内の手動実装を兼ねる末端（TC-IC-048。docs/概要.md §8 の許容形。編集ラウンド）",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(siGeneratedManual, IcTestSupport.classDigests(dir).filterKeys(IcBasicFixture::isSiGenerated))

        // 復元で clean 基準とバイト一致まで戻る
        TestKitHarness.writeFile(dir, IcBasicFixture.FOO_FILE, originalFoo)
        TestKitHarness.writeFile(dir, IcBasicFixture.USE_FILE, originalUse)
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // 新規 @Enumize 階層の追加（TC-IC-042）: 追加ファイルだけがコンパイルされ、既存の SI / TI 両階層の
    // 出力は書き直されない（P3: 述語マッチ由来の全 @Enumize 再コンパイル税が無いこと）
    @Test
    fun case42NewEnumizeHierarchyDoesNotDirtyExistingOnes() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic42-")
        TestKitHarness.build(dir, "compileKotlin")
        val times0 = IcTestSupport.classTimes(dir)

        TestKitHarness.writeFile(
            dir, IcBasicFixture.THIRD_FILE,
            "package org.wrongwrong.icfix\n\nimport org.wrongwrong.sealedClassEnumizer.Enumize\n\n" +
                "// 新規追加される第 3 の @Enumize 階層（TC-IC-042: 既存階層へ dirty 税が及ばないこと）\n" +
                "@Enumize\nsealed interface SI3 {\n    data object W3 : SI3\n}\n",
        )
        TestKitHarness.build(dir, "compileKotlin")
        // モジュール単位の成果物（META-INF/*.kotlin_module）はコンパイルの度に書き直されるため、
        // クラス出力（ファイル単位の帰属を持つもの）だけを dirty 税の判定対象にする
        val changed = IcTestSupport.changedKeys(times0, IcTestSupport.classTimes(dir))
            .filterTo(mutableSetOf()) { !it.startsWith("META-INF/") }

        assertTrue(changed.isNotEmpty() && changed.all { "/SI3" in it }, "追加階層の出力のみが書かれること: $changed")
        assertTrue(changed.none(IcBasicFixture::isTiOutput), "TI 出力は不変であること: $changed")
        assertTrue(changed.none(IcBasicFixture::isSiGenerated), "SI 生成物は不変であること: $changed")
    }
}
