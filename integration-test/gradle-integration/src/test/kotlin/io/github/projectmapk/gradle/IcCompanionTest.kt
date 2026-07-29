package io.github.projectmapk.gradle

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

// IC 回帰マトリクス（companion・手動実装・dirty 税 = docs/test/ケース06-ビルド動態.md
// BLD-15〜18・BLD-22/24/25）。ic-basic の #7 系・#12・階層内手動実装・P3 と、
// ic-denotable の規則 3 トグル（BLD-18）を担う
class IcCompanionTest {
    private val fooCompanionClass = "${IcBasicFixture.CLASS_PREFIX}/Foo\$Companion.class"
    private val fooFactoryClass = "${IcBasicFixture.CLASS_PREFIX}/Foo\$Factory.class"

    // docs/test/ケース06-ビルド動態.md BLD-15: #7 既定名 companion の増減は自動生成⇔流用の透過で
    // OUT・クラス名（Foo$Companion）不変・復帰でバイト一致
    @Test
    fun case7DefaultCompanionToggleKeepsShape() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic7-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)
        assertTrue(fooCompanionClass in digests0.keys, "自動生成 companion のクラスが存在すること")

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.FOO_FILE,
            "class Foo(val v: Int) : SI",
            "class Foo(val v: Int) : SI {\n    companion object\n}",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertTrue(
            fooCompanionClass in IcTestSupport.classDigests(dir).keys,
            "手動 companion でもクラス名は不変",
        )

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.FOO_FILE,
            "class Foo(val v: Int) : SI {\n    companion object\n}",
            "class Foo(val v: Int) : SI",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // docs/test/ケース06-ビルド動態.md BLD-16: #7-b 名前付き companion 化の ABI 差分は
    // 修飾名指し側のみ・label / valueOf 不変・旧クラスの stale 掃除
    @Test
    fun case7bNamedCompanionSwitchAffectsQualifiedReferencesOnly() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic7b-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.FOO_FILE,
            "class Foo(val v: Int) : SI",
            "class Foo(val v: Int) : SI {\n    companion object Factory\n}",
        )
        val failure = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue(
            "Companion" in failure.output,
            "修飾名 Foo.Companion の名指しが追従を要すること:\n${failure.output}",
        )

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.USE_FILE,
            "Foo.Companion -> \"foo\"",
            "Foo.Factory -> \"foo\"",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val keys = IcTestSupport.classDigests(dir).keys
        assertTrue(fooFactoryClass in keys, "Foo\$Factory.class が生成されること")
        assertTrue(fooCompanionClass !in keys, "旧 Foo\$Companion.class の stale が掃除されること")
    }

    // docs/test/ケース06-ビルド動態.md BLD-17: #7-c companion 可視性トグルで asEnumish 返り値型が
    // 規則 1（具体型）⇔ 規則 2（SI.Enumish）で切り替わり、private 化は IR-only アクセサで build 成立
    @Test
    fun case7cCompanionVisibilityToggleSwitchesReturnTypeAndAccessor() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic7c-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)

        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.FOO_FILE,
            "class Foo(val v: Int) : SI",
            "class Foo(val v: Int) : SI {\n    companion object\n}",
        )
        TestKitHarness.writeFile(
            dir,
            IcBasicFixture.TYPED_USE_FILE,
            "package io.github.projectmapk.icfix\n\n// asEnumish の返り値型（規則 1: 具体型 Foo.Companion）へ静的に依存する観測点\nprivate val typedKind: Foo.Companion = Foo(1).asEnumish()\n",
        )
        TestKitHarness.build(dir, "compileKotlin")

        // internal 化 → 返り値型が SI.Enumish へフォールバック（規則 2）し、具体型依存が壊れる
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.FOO_FILE,
            "    companion object\n",
            "    internal companion object\n",
        )
        val mismatch = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue("Enumish" in mismatch.output, "返り値型の切替が型不一致として観測されること:\n${mismatch.output}")

        TestKitHarness.writeFile(
            dir,
            IcBasicFixture.TYPED_USE_FILE,
            "package io.github.projectmapk.icfix\n\n// 規則 2 フォールバック後の返り値型（SI.Enumish）に合わせた観測点\nprivate val typedKind: SI.Enumish = Foo(1).asEnumish()\n",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))

        // private 化 → 規則 2 のまま entries 構築は IR-only アクセサで成立し build も実行も通る。
        // 利用側の kind 名指し（Foo.Companion 枝）は private では言語不能のため else 受けへ追随する
        val originalUse = IcTestSupport.readFile(dir, IcBasicFixture.USE_FILE)
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.FOO_FILE,
            "    internal companion object\n",
            "    private companion object\n",
        )
        TestKitHarness.writeFile(
            dir,
            IcBasicFixture.USE_FILE,
            "package io.github.projectmapk.icfix\n\n// private kind は名指し不能のため else で受ける一時形（#7-c private 化ラウンド）\nfun describe(value: SI): String = when (value.asEnumish()) {\n    Bar -> \"bar\"\n    Outer.Leaf -> \"leaf\"\n    else -> \"foo\"\n}\n",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))

        // 逆編集で public 具体型（規則 1）へ復帰する
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.FOO_FILE,
            "    private companion object\n",
            "    companion object\n",
        )
        TestKitHarness.writeFile(dir, IcBasicFixture.USE_FILE, originalUse)
        TestKitHarness.writeFile(
            dir,
            IcBasicFixture.TYPED_USE_FILE,
            "package io.github.projectmapk.icfix\n\n// asEnumish の返り値型（規則 1: 具体型 Foo.Companion）へ静的に依存する観測点\nprivate val typedKind: Foo.Companion = Foo(1).asEnumish()\n",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
    }

    // docs/test/ケース06-ビルド動態.md BLD-18: #7-c × 広い末端 = companion internal 化で
    // KIND_TYPE_NOT_DENOTABLE（規則 3）が発火し、逆編集で解除される（ic-denotable フィクスチャ）
    @Test
    fun widerLeafCompanionNarrowingTogglesNotDenotable() {
        val dir = IcTestSupport.prepare("ic-denotable", "icden-")
        val wideFile = "src/main/kotlin/io/github/projectmapk/widerfix/Wide.kt"
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(listOf("WENTRIES=Wide", "WKIND=Wide"), baseline)

        TestKitHarness.replaceInFile(
            dir,
            wideFile,
            "    companion object\n",
            "    internal companion object\n",
        )
        val failure = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertTrue(
            "cannot be denoted" in failure.output,
            "ENUMIZE_KIND_TYPE_NOT_DENOTABLE の発火:\n${failure.output}",
        )

        TestKitHarness.replaceInFile(
            dir,
            wideFile,
            "    internal companion object\n",
            "    companion object\n",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
    }

    // docs/test/ケース06-ビルド動態.md BLD-24: #12 階層外手動実装は追加ファイル単体ラウンドで
    // MANUAL_IMPL_OUTSIDE_HIERARCHY が顕在化し、削除で clean バイト一致まで復帰する
    @Test
    fun case12OutsideManualImplToggleRestoresBytes() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic12-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)

        TestKitHarness.writeFile(
            dir,
            IcBasicFixture.ROGUE_FILE,
            "package io.github.projectmapk.icfix\n\nimport kotlin.reflect.KClass\n\n" +
                "// 階層外の手動実装（#12 = docs/test/ケース06-ビルド動態.md BLD-24。単体ラウンドでの発火観測）\n" +
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

    // docs/test/ケース06-ビルド動態.md BLD-25: 階層内手動実装への切替で entries 不変・kind-when へ
    // is 枝要求・非 ABI 編集で安定・復元で clean バイト一致
    @Test
    fun hierarchyInternalManualImplEditKeepsEntries() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic25-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)
        val originalFoo = IcTestSupport.readFile(dir, IcBasicFixture.FOO_FILE)
        val originalUse = IcTestSupport.readFile(dir, IcBasicFixture.USE_FILE)

        // 末端 Foo を「末端でもあり手動実装でもある」形へ（Enumish 由来 label のため ES 除外）
        TestKitHarness.writeFile(
            dir,
            IcBasicFixture.FOO_FILE,
            "package io.github.projectmapk.icfix\n\nimport kotlin.reflect.KClass\n\n" +
                "// 階層内の手動実装を兼ねる末端（docs/test/ケース06-ビルド動態.md BLD-25 の許容形）\n" +
                "class Foo(val v: Int) : SI, SI.Enumish {\n" +
                "    override val label: String get() = \"FooManual\"\n\n" +
                "    override val enumizedClass: KClass<out SI> get() = Foo::class\n" +
                "}\n",
        )
        // 手動実装が inheritors に載るため kind-when へ is Foo 枝が要る
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.USE_FILE,
            "    Outer.Leaf -> \"leaf\"",
            "    Outer.Leaf -> \"leaf\"\n    is Foo -> \"fooManual\"",
        )
        val manual = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        // 手動実装値は kind でないため entries / valueOf / describe の実行時結果は不変
        assertEquals(baseline, manual)
        val siGeneratedManual =
            IcTestSupport.classDigests(dir).filterKeys(IcBasicFixture::isSiGenerated)

        // 手動実装ファイルの ABI 非変更編集 → 階層共連れで inheritors 再計算・結果は安定
        TestKitHarness.replaceInFile(
            dir,
            IcBasicFixture.FOO_FILE,
            "// 階層内の手動実装を兼ねる末端（docs/test/ケース06-ビルド動態.md BLD-25 の許容形）",
            "// 階層内の手動実装を兼ねる末端（docs/test/ケース06-ビルド動態.md BLD-25 の許容形。編集ラウンド）",
        )
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(
            siGeneratedManual,
            IcTestSupport.classDigests(dir).filterKeys(IcBasicFixture::isSiGenerated),
        )

        // 復元で clean 基準とバイト一致まで戻る
        TestKitHarness.writeFile(dir, IcBasicFixture.FOO_FILE, originalFoo)
        TestKitHarness.writeFile(dir, IcBasicFixture.USE_FILE, originalUse)
        assertEquals(baseline, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // docs/test/ケース06-ビルド動態.md BLD-22: 新規 @Enumize 階層ファイルの追加は既存階層の出力を
    // 再生成しない（P3: 述語マッチ由来の全 @Enumize 再コンパイル税なし）
    @Test
    fun newHierarchyAdditionDoesNotDirtyExisting() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "ic22n-")
        TestKitHarness.build(dir, "compileKotlin")
        val times0 = IcTestSupport.classTimes(dir)

        TestKitHarness.writeFile(
            dir,
            IcBasicFixture.THIRD_FILE,
            "package io.github.projectmapk.icfix\n\nimport io.github.projectmapk.sealedClassEnumizer.Enumize\n\n" +
                "// 新規追加される第 3 の @Enumize 階層（docs/test/ケース06-ビルド動態.md BLD-22: 既存階層へ dirty 税が及ばない）\n" +
                "@Enumize\nsealed interface SI3 {\n    data object W3 : SI3\n}\n",
        )
        TestKitHarness.build(dir, "compileKotlin")
        // モジュール単位の成果物（META-INF/*.kotlin_module）はコンパイルの度に書き直されるため、
        // クラス出力（ファイル単位の帰属を持つもの）だけを dirty 税の判定対象にする
        val changed =
            IcTestSupport.changedKeys(times0, IcTestSupport.classTimes(dir)).filterTo(
                mutableSetOf()
            ) {
                !it.startsWith("META-INF/")
            }

        assertTrue(
            changed.isNotEmpty() && changed.all { "/SI3" in it },
            "追加階層の出力のみが書かれること: $changed",
        )
        assertTrue(changed.none(IcBasicFixture::isTiOutput), "TI 出力は不変であること: $changed")
        assertTrue(changed.none(IcBasicFixture::isSiGenerated), "SI 生成物は不変であること: $changed")
        assertTrue(changed.none(IcBasicFixture::isNbGenerated), "NB 生成物は不変であること: $changed")
    }
}
