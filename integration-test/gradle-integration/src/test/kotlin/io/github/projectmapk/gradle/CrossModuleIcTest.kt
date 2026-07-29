package io.github.projectmapk.gradle

import io.github.projectmapk.gradle.DiagAsserts.assertFragmentAbsent
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

// 跨 module の IC・決定性の掃討（docs/test/ケース04-診断.md DIA-20・
// docs/test/ケース06-ビルド動態.md BLD-36〜39・BLD-46）。
// diag-cross-absorb / abi-propagation フィクスチャを展開先コピー上の編集で再利用する
class CrossModuleIcTest {
    private companion object {
        const val ABSORB_APP_SQUARE = "app/src/main/kotlin/io/github/projectmapk/diag/xabapp/Sq.kt"
        const val ABI_SI = "producer/src/main/kotlin/io/github/projectmapk/abifix/Si.kt"
        const val ABI_FOO = "producer/src/main/kotlin/io/github/projectmapk/abifix/Foo.kt"
        const val ABI_AOO = "producer/src/main/kotlin/io/github/projectmapk/abifix/Aoo.kt"
        const val ABI_OUTER = "producer/src/main/kotlin/io/github/projectmapk/abifix/Outer.kt"
        const val ABI_AOUTER = "producer/src/main/kotlin/io/github/projectmapk/abifix/AOuter.kt"
        const val ABI_MID = "producer/src/main/kotlin/io/github/projectmapk/abifix/AMid.kt"
        const val ABI_TYPED = "consumer/src/main/kotlin/io/github/projectmapk/abiuse/Typed.kt"
        const val ABI_USE = "consumer/src/main/kotlin/io/github/projectmapk/abiuse/Use.kt"
        const val ABI_MAIN = "consumer/src/main/kotlin/io/github/projectmapk/abiuse/Main.kt"
        const val ABI_QUALIFIED =
            "consumer/src/main/kotlin/io/github/projectmapk/abiuse/Qualified.kt"
        const val ABI_SHORT_WHEN =
            "consumer/src/main/kotlin/io/github/projectmapk/abiuse/ShortWhen.kt"
    }

    // docs/test/ケース04-診断.md DIA-20: 別 module の単一サブタイプも吸収され AK / IL 非発火
    @Test
    fun crossModuleSingleSubtypeIsAbsorbed() {
        val dir = IcTestSupport.prepare("diag-cross-absorb", "xmabs-")
        val result = TestKitHarness.build(dir, ":app:compileKotlin")
        assertFragmentAbsent(result.output, DiagFragments.AMBIGUOUS_KIND)
        assertFragmentAbsent(result.output, DiagFragments.INNER_LEAF)
    }

    // docs/test/ケース06-ビルド動態.md BLD-36: #7-c 跨 module = companion internal 化で consumer の
    // 具体型依存がコンパイルエラーになり、逆編集で復帰する（返り値型切替の ABI 伝播）
    @Test
    fun companionVisibilityTogglePropagatesReturnTypeAcrossModules() {
        val dir = IcTestSupport.prepare("abi-propagation", "xm36-")
        TestKitHarness.writeFile(
            dir,
            ABI_TYPED,
            "package io.github.projectmapk.abiuse\n\nimport io.github.projectmapk.abifix.Foo\n\n" +
                "// asEnumish の返り値型（規則 1: 具体型 Foo.Companion）へ静的に依存する観測点\n" +
                "fun typedProbe(): String {\n" +
                "    val kind: Foo.Companion = Foo(3).asEnumish()\n" +
                "    return kind.label\n" +
                "}\n",
        )
        TestKitHarness.build(dir, ":consumer:compileKotlin")

        TestKitHarness.replaceInFile(
            dir,
            ABI_FOO,
            "class Foo(val v: Int) : SI",
            "class Foo(val v: Int) : SI {\n    internal companion object\n}",
        )
        val failure = TestKitHarness.buildAndFail(dir, ":consumer:compileKotlin")
        assertTrue(
            failure.output.lineSequence().any { it.contains("Typed.kt:") && it.contains("e: ") },
            "規則 2 への切替が consumer の具体型依存を壊すこと:\n${failure.output}",
        )

        TestKitHarness.replaceInFile(
            dir,
            ABI_FOO,
            "class Foo(val v: Int) : SI {\n    internal companion object\n}",
            "class Foo(val v: Int) : SI",
        )
        val restored = TestKitHarness.build(dir, ":consumer:compileKotlin")
        assertTrue(restored.output.contains("BUILD SUCCESSFUL"), "逆編集で規則 1 へ復帰すること")
    }

    // docs/test/ケース06-ビルド動態.md BLD-37: internal 中間 sealed 挿入 + 末端付け替えで consumer の
    // entries 並びが変化し、kind 集合は不変のため kind-when は壊れない（波及の最小性）
    @Test
    fun intermediateInsertionReordersEntriesAcrossModules() {
        val dir = IcTestSupport.prepare("abi-propagation", "xm37-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain"))
        assertEquals(listOf("ENTRIES=Bar,Foo", "KINDS=bar,foo"), baseline)

        TestKitHarness.writeFile(
            dir,
            ABI_MID,
            "package io.github.projectmapk.abifix\n\n" +
                "// 非入れ子の internal 中間 sealed（docs/test/ケース06-ビルド動態.md BLD-37。中間には何も生成されない）\n" +
                "internal sealed interface AMid : SI\n",
        )
        TestKitHarness.replaceInFile(
            dir,
            ABI_FOO,
            "class Foo(val v: Int) : SI",
            "class Foo(val v: Int) : AMid",
        )
        val second = TestKitHarness.build(dir, ":consumer:runMain")

        // SI 継承者 [AMid, Bar] → AMid を [Foo] へ展開 → entries=[Foo, Bar]（入れ子展開順）
        assertEquals(listOf("ENTRIES=Foo,Bar", "KINDS=foo,bar"), IcTestSupport.outLines(second))
    }

    // docs/test/ケース06-ビルド動態.md BLD-38: producer のコメントのみ編集で producer バイト一致・
    // consumer UP-TO-DATE（跨 module 決定性）
    @Test
    fun commentOnlyProducerEditKeepsConsumerUpToDate() {
        val dir = IcTestSupport.prepare("abi-propagation", "xm38-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain"))
        val producerDigests = IcTestSupport.classDigests(dir, "producer")

        TestKitHarness.replaceInFile(
            dir,
            ABI_SI,
            "// 跨モジュール ABI 伝播フィクスチャの基底（末端は別ファイル）",
            "// 跨モジュール ABI 伝播フィクスチャの基底（末端は別ファイル・コメントのみ編集）",
        )
        val second = TestKitHarness.build(dir, ":consumer:runMain")

        assertEquals(baseline, IcTestSupport.outLines(second))
        assertEquals(producerDigests, IcTestSupport.classDigests(dir, "producer"))
        assertEquals(TaskOutcome.UP_TO_DATE, second.task(":consumer:compileKotlin")?.outcome)
    }

    // docs/test/ケース06-ビルド動態.md BLD-39: consumer 側サブタイプ追加は producer を dirty にしない
    // （サブタイプは階層外・entries 非参加のため producer の生成物はバイト不変）
    @Test
    fun downstreamSubtypeAdditionDoesNotDirtyProducer() {
        val dir = IcTestSupport.prepare("diag-cross-absorb", "xm39-")
        TestKitHarness.build(dir, ":app:compileKotlin")
        val libDigests = IcTestSupport.classDigests(dir, "lib")

        TestKitHarness.writeFile(
            dir,
            ABSORB_APP_SQUARE,
            "package io.github.projectmapk.diag.xabapp\n\nimport io.github.projectmapk.diag.xab.XabSi\n\n" +
                "// 追加のサブタイプ（階層外。producer の entries / 生成物へ影響しない = docs/test/ケース06-ビルド動態.md BLD-39）\n" +
                "class Sq : XabSi.Poly()\n",
        )
        val second = TestKitHarness.build(dir, ":app:compileKotlin")

        assertEquals(TaskOutcome.UP_TO_DATE, second.task(":lib:compileKotlin")?.outcome)
        assertEquals(libDigests, IcTestSupport.classDigests(dir, "lib"))
    }

    // docs/test/ケース06-ビルド動態.md BLD-46: #6a 末端改名の跨 module 波及。未追従 consumer の
    // 名指し枝の未解決エラーが同編集の clean と一致し（失敗パリティ）、追従後は並びと label が
    // 変わって valueOf(旧名) が実行時 IAE になり、生成物は同状態 clean とバイト一致・全復元で基準一致
    @Test
    fun renameLeafPropagatesOrderAndLabelAcrossModules() {
        val dir = IcTestSupport.prepare("abi-propagation", "xm46a-")
        TestKitHarness.writeFile(
            dir,
            ABI_MAIN,
            mainWithProbes(valueOfProbeLine("PROBE_FOO", "Foo")),
        )
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain"))
        assertEquals(listOf("ENTRIES=Bar,Foo", "KINDS=bar,foo", "PROBE_FOO=Foo"), baseline)
        val producer0 = IcTestSupport.classDigests(dir, "producer")
        val consumer0 = IcTestSupport.classDigests(dir, "consumer")

        // producer の Foo → Aoo（宣言とファイル名を追従。producer 内の他参照は無い）
        renameFooToAoo(dir)
        val incrementalFailure = TestKitHarness.buildAndFail(dir, ":consumer:compileKotlin")
        assertEquals(
            TaskOutcome.SUCCESS,
            incrementalFailure.task(":producer:compileKotlin")?.outcome,
        )
        assertEquals(
            TaskOutcome.FAILED,
            incrementalFailure.task(":consumer:compileKotlin")?.outcome,
        )
        val incrementalErrors = fileErrors(incrementalFailure, "Use.kt")
        assertTrue(
            incrementalErrors.isNotEmpty(),
            "Foo 名指しの枝が未解決になること:\n${incrementalFailure.output}",
        )

        // 失敗パリティ: 同じ改名済みソースを最初からビルドする clean 側と同一エラー
        val cleanDir = IcTestSupport.prepare("abi-propagation", "xm46ac-")
        TestKitHarness.writeFile(
            cleanDir,
            ABI_MAIN,
            mainWithProbes(valueOfProbeLine("PROBE_FOO", "Foo")),
        )
        renameFooToAoo(cleanDir)
        val cleanFailure = TestKitHarness.buildAndFail(cleanDir, ":consumer:compileKotlin")
        assertEquals(fileErrors(cleanFailure, "Use.kt"), incrementalErrors)

        // consumer の枝を Aoo へ追従 → 並びと label が変化し valueOf("Foo") は実行時 IAE
        val renamedOut =
            listOf(
                "ENTRIES=Aoo,Bar",
                "KINDS=foo,bar",
                "PROBE_FOO=IAE:No enumish entry with label 'Foo' in SI",
            )
        followUseToAoo(dir)
        assertEquals(
            renamedOut,
            IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain")),
        )

        // 同状態 clean と生成物バイト一致（stale 掃除まで含めた収束の合否条件）
        followUseToAoo(cleanDir)
        assertEquals(
            renamedOut,
            IcTestSupport.outLines(TestKitHarness.build(cleanDir, ":consumer:runMain")),
        )
        assertEquals(
            IcTestSupport.classDigests(cleanDir, "producer"),
            IcTestSupport.classDigests(dir, "producer"),
        )
        assertEquals(
            IcTestSupport.classDigests(cleanDir, "consumer"),
            IcTestSupport.classDigests(dir, "consumer"),
        )

        // 全復元 → 基準の OUT・生成物バイトへ一致
        TestKitHarness.replaceInFile(
            dir,
            ABI_AOO,
            "class Aoo(val v: Int) : SI",
            "class Foo(val v: Int) : SI",
        )
        IcTestSupport.moveFile(dir, ABI_AOO, ABI_FOO)
        TestKitHarness.replaceInFile(
            dir,
            ABI_USE,
            "import io.github.projectmapk.abifix.Aoo",
            "import io.github.projectmapk.abifix.Foo",
        )
        TestKitHarness.replaceInFile(
            dir,
            ABI_USE,
            "Aoo.Companion -> \"foo\"",
            "Foo.Companion -> \"foo\"",
        )
        assertEquals(
            baseline,
            IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain")),
        )
        assertEquals(producer0, IcTestSupport.classDigests(dir, "producer"))
        assertEquals(consumer0, IcTestSupport.classDigests(dir, "consumer"))
    }

    // docs/test/ケース06-ビルド動態.md BLD-46: #6b 外側クラス改名の跨 module 波及は並びのみで、
    // label "Leaf" と valueOf("Leaf") は不変・同状態 clean と生成物バイト一致・復元で基準一致
    @Test
    fun renameOuterKeepsLabelReordersEntriesAcrossModules() {
        val dir = IcTestSupport.prepare("abi-propagation", "xm46b-")
        setupOuterLeaf(dir)
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain"))
        assertEquals(
            listOf("ENTRIES=Bar,Foo,Leaf", "KINDS=bar,foo,leaf", "PROBE_LEAF=Leaf"),
            baseline,
        )
        val producer0 = IcTestSupport.classDigests(dir, "producer")
        val consumer0 = IcTestSupport.classDigests(dir, "consumer")

        // Outer → AOuter（consumer の名指し枝も追従）→ FQN 先頭が変わり並びのみ再配置
        renameOuterToAOuter(dir)
        val renamedOut = listOf("ENTRIES=Leaf,Bar,Foo", "KINDS=leaf,bar,foo", "PROBE_LEAF=Leaf")
        assertEquals(
            renamedOut,
            IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain")),
        )

        // 同状態 clean と生成物バイト一致
        val cleanDir = IcTestSupport.prepare("abi-propagation", "xm46bc-")
        setupOuterLeaf(cleanDir)
        renameOuterToAOuter(cleanDir)
        assertEquals(
            renamedOut,
            IcTestSupport.outLines(TestKitHarness.build(cleanDir, ":consumer:runMain")),
        )
        assertEquals(
            IcTestSupport.classDigests(cleanDir, "producer"),
            IcTestSupport.classDigests(dir, "producer"),
        )
        assertEquals(
            IcTestSupport.classDigests(cleanDir, "consumer"),
            IcTestSupport.classDigests(dir, "consumer"),
        )

        // 復元 → 基準の OUT・生成物バイトへ一致
        TestKitHarness.replaceInFile(dir, ABI_AOUTER, "class AOuter {", "class Outer {")
        IcTestSupport.moveFile(dir, ABI_AOUTER, ABI_OUTER)
        TestKitHarness.replaceInFile(
            dir,
            ABI_USE,
            "import io.github.projectmapk.abifix.AOuter",
            "import io.github.projectmapk.abifix.Outer",
        )
        TestKitHarness.replaceInFile(
            dir,
            ABI_USE,
            "AOuter.Leaf -> \"leaf\"",
            "Outer.Leaf -> \"leaf\"",
        )
        assertEquals(
            baseline,
            IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain")),
        )
        assertEquals(producer0, IcTestSupport.classDigests(dir, "producer"))
        assertEquals(consumer0, IcTestSupport.classDigests(dir, "consumer"))
    }

    // docs/test/ケース06-ビルド動態.md BLD-46: #7-b companion 改名の跨 module 波及は修飾名
    // 名指し側のみ（短縮形 when・label・valueOf は不変）。追従で green・復元で基準一致
    @Test
    fun namedCompanionSwitchPropagatesOnlyToQualifiedNamesAcrossModules() {
        val dir = IcTestSupport.prepare("abi-propagation", "xm46c-")
        setupNamedCompanionProbes(dir)
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain"))
        assertEquals(
            listOf(
                "ENTRIES=Bar,Foo",
                "KINDS=bar,foo",
                "SHORT=bar,foo",
                "QUALIFIED=Foo",
                "VALUEOF=Foo",
            ),
            baseline,
        )
        val producer0 = IcTestSupport.classDigests(dir, "producer")
        val consumer0 = IcTestSupport.classDigests(dir, "consumer")

        // companion object Factory 化 → 修飾名 Foo.Companion の名指し側だけが壊れる
        TestKitHarness.replaceInFile(
            dir,
            ABI_FOO,
            "    companion object\n",
            "    companion object Factory\n",
        )
        val failure = TestKitHarness.buildAndFail(dir, ":consumer:compileKotlin")
        assertEquals(TaskOutcome.SUCCESS, failure.task(":producer:compileKotlin")?.outcome)
        assertTrue(
            fileErrors(failure, "Qualified.kt").isNotEmpty(),
            "修飾名の val 参照が追従を要すること:\n${failure.output}",
        )
        assertTrue(
            fileErrors(failure, "Use.kt").isNotEmpty(),
            "修飾名の when 枝が追従を要すること:\n${failure.output}",
        )
        assertEquals(emptyList(), fileErrors(failure, "ShortWhen.kt"), "短縮形 when は追従不要のこと")
        assertEquals(emptyList(), fileErrors(failure, "Main.kt"), "label / valueOf 利用は追従不要のこと")

        // 修飾名参照を Foo.Factory へ追従 → OUT は基準と一致（短縮形・label・valueOf 不変）
        TestKitHarness.replaceInFile(
            dir,
            ABI_USE,
            "Foo.Companion -> \"foo\"",
            "Foo.Factory -> \"foo\"",
        )
        TestKitHarness.replaceInFile(
            dir,
            ABI_QUALIFIED,
            "private val qualifiedKind: Foo.Companion = Foo.Companion",
            "private val qualifiedKind: Foo.Factory = Foo.Factory",
        )
        assertEquals(
            baseline,
            IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain")),
        )
        val producerKeys = IcTestSupport.classDigests(dir, "producer").keys
        assertTrue(
            "io/github/projectmapk/abifix/Foo\$Factory.class" in producerKeys,
            "Foo\$Factory.class が生成されること: $producerKeys",
        )
        assertTrue(
            "io/github/projectmapk/abifix/Foo\$Companion.class" !in producerKeys,
            "旧 Foo\$Companion.class の stale が掃除されること: $producerKeys",
        )

        // 復元 → 基準の OUT・生成物バイトへ一致
        TestKitHarness.replaceInFile(
            dir,
            ABI_FOO,
            "    companion object Factory\n",
            "    companion object\n",
        )
        TestKitHarness.replaceInFile(
            dir,
            ABI_USE,
            "Foo.Factory -> \"foo\"",
            "Foo.Companion -> \"foo\"",
        )
        TestKitHarness.replaceInFile(
            dir,
            ABI_QUALIFIED,
            "private val qualifiedKind: Foo.Factory = Foo.Factory",
            "private val qualifiedKind: Foo.Companion = Foo.Companion",
        )
        assertEquals(
            baseline,
            IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain")),
        )
        assertEquals(producer0, IcTestSupport.classDigests(dir, "producer"))
        assertEquals(consumer0, IcTestSupport.classDigests(dir, "consumer"))
    }

    // BLD-46 #6a: producer 側の末端改名（宣言とファイル名の追従）
    private fun renameFooToAoo(dir: Path) {
        TestKitHarness.replaceInFile(
            dir,
            ABI_FOO,
            "class Foo(val v: Int) : SI",
            "class Aoo(val v: Int) : SI",
        )
        IcTestSupport.moveFile(dir, ABI_FOO, ABI_AOO)
    }

    // BLD-46 #6a: consumer 側の名指し枝の追従
    private fun followUseToAoo(dir: Path) {
        TestKitHarness.replaceInFile(
            dir,
            ABI_USE,
            "import io.github.projectmapk.abifix.Foo",
            "import io.github.projectmapk.abifix.Aoo",
        )
        TestKitHarness.replaceInFile(
            dir,
            ABI_USE,
            "Foo.Companion -> \"foo\"",
            "Aoo.Companion -> \"foo\"",
        )
    }

    // BLD-46 #6b のシナリオ内セットアップ。既存メソッドの基準（ENTRIES=Bar,Foo）を保つため、
    // 外側クラス内ネスト末端は基底フィクスチャへ置かずシナリオ内で追加する
    private fun setupOuterLeaf(dir: Path) {
        TestKitHarness.writeFile(
            dir,
            ABI_OUTER,
            "package io.github.projectmapk.abifix\n\n" +
                "// 階層外の外側クラス内ネスト末端（#6b 改名対象 = docs/test/ケース06-ビルド動態.md BLD-46）\n" +
                "class Outer {\n    data object Leaf : SI\n}\n",
        )
        TestKitHarness.replaceInFile(
            dir,
            ABI_USE,
            "import io.github.projectmapk.abifix.Foo",
            "import io.github.projectmapk.abifix.Foo\nimport io.github.projectmapk.abifix.Outer",
        )
        TestKitHarness.replaceInFile(
            dir,
            ABI_USE,
            "    Bar -> \"bar\"",
            "    Bar -> \"bar\"\n    Outer.Leaf -> \"leaf\"",
        )
        TestKitHarness.writeFile(
            dir,
            ABI_MAIN,
            mainWithProbes(valueOfProbeLine("PROBE_LEAF", "Leaf")),
        )
    }

    // BLD-46 #6b: 外側クラス改名と consumer の名指し枝の追従
    private fun renameOuterToAOuter(dir: Path) {
        TestKitHarness.replaceInFile(dir, ABI_OUTER, "class Outer {", "class AOuter {")
        IcTestSupport.moveFile(dir, ABI_OUTER, ABI_AOUTER)
        TestKitHarness.replaceInFile(
            dir,
            ABI_USE,
            "import io.github.projectmapk.abifix.Outer",
            "import io.github.projectmapk.abifix.AOuter",
        )
        TestKitHarness.replaceInFile(
            dir,
            ABI_USE,
            "Outer.Leaf -> \"leaf\"",
            "AOuter.Leaf -> \"leaf\"",
        )
    }

    // BLD-46 #7-b のシナリオ内セットアップ: producer の Foo へ手動 companion を置き、
    // consumer へ修飾名参照（val）と短縮形 when を併置して観測面を分離する
    private fun setupNamedCompanionProbes(dir: Path) {
        TestKitHarness.replaceInFile(
            dir,
            ABI_FOO,
            "class Foo(val v: Int) : SI",
            "class Foo(val v: Int) : SI {\n    companion object\n}",
        )
        TestKitHarness.writeFile(
            dir,
            ABI_QUALIFIED,
            "package io.github.projectmapk.abiuse\n\nimport io.github.projectmapk.abifix.Foo\n\n" +
                "// 修飾名 Foo.Companion への静的依存（companion 改名で追従を要する側 = " +
                "docs/test/ケース06-ビルド動態.md BLD-46）\n" +
                "private val qualifiedKind: Foo.Companion = Foo.Companion\n\n" +
                "fun qualifiedLabel(): String = qualifiedKind.label\n",
        )
        TestKitHarness.writeFile(
            dir,
            ABI_SHORT_WHEN,
            "package io.github.projectmapk.abiuse\n\nimport io.github.projectmapk.abifix.Bar\n" +
                "import io.github.projectmapk.abifix.Foo\nimport io.github.projectmapk.abifix.SI\n\n" +
                "// 短縮形の kind-when（companion 改名の影響を受けない側 = " +
                "docs/test/ケース06-ビルド動態.md BLD-46）\n" +
                "fun shortKind(kind: SI.Enumish): String = when (kind) {\n" +
                "    Foo -> \"foo\"\n" +
                "    Bar -> \"bar\"\n" +
                "}\n",
        )
        TestKitHarness.writeFile(
            dir,
            ABI_MAIN,
            mainWithProbes(
                "println(\"OUT:SHORT=\" + entries.joinToString(\",\") { shortKind(it) })",
                "println(\"OUT:QUALIFIED=\" + qualifiedLabel())",
                "println(\"OUT:VALUEOF=\" + SI.Enumish.valueOf(\"Foo\").label)",
            ),
        )
    }

    // BLD-46 の各シナリオ用: 基本観測（ENTRIES / KINDS）へプローブ println 行を加えた Main.kt
    private fun mainWithProbes(vararg probeLines: String): String =
        "package io.github.projectmapk.abiuse\n\nimport io.github.projectmapk.abifix.SI\n\n" +
            "// 跨モジュールの実行時観測（docs/test/ケース06-ビルド動態.md BLD-46 のプローブ付き差し替え）\n" +
            "fun main() {\n" +
            "    val entries = SI.Enumish.entries\n" +
            "    println(\"OUT:ENTRIES=\" + entries.joinToString(\",\") { it.label })\n" +
            "    println(\"OUT:KINDS=\" + entries.joinToString(\",\") { describeKind(it) })\n" +
            probeLines.joinToString("") { "    $it\n" } +
            "}\n"

    // valueOf(label) の解決/実行時 IAE を 1 行で観測するプローブ
    private fun valueOfProbeLine(out: String, label: String): String =
        "println(\"OUT:$out=\" + try { SI.Enumish.valueOf(\"$label\").label } " +
            "catch (e: IllegalArgumentException) { \"IAE:\" + e.message })"

    // 失敗パリティ・影響面の照合用: 指定ファイルへ帰属するエラー行を絶対パス差を除いた形で抽出する
    private fun fileErrors(result: BuildResult, fileName: String): List<String> =
        result.output
            .lineSequence()
            .filter { "e: " in it && fileName in it }
            .map { it.substring(it.indexOf(fileName)).trim() }
            .distinct()
            .toList()
}
