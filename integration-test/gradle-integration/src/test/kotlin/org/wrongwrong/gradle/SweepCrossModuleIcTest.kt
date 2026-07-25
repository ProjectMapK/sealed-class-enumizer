package org.wrongwrong.gradle

import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// 残ケース掃討: 跨モジュール IC・決定性（既存フィクスチャ diag-cross-absorb / abi-propagation を
// 展開先コピー上の編集で再利用する。docs/テストケース管理.md TC-XM-019/038/055・TC-VIS-047/050・
// TC-GAP-016 の IC 面）
class SweepCrossModuleIcTest {
    private companion object {
        const val ABSORB_APP_SQUARE = "app/src/main/kotlin/org/wrongwrong/diag/xabapp/Sq.kt"
        const val ABI_SI = "producer/src/main/kotlin/org/wrongwrong/abifix/Si.kt"
        const val ABI_FOO = "producer/src/main/kotlin/org/wrongwrong/abifix/Foo.kt"
        const val ABI_MID = "producer/src/main/kotlin/org/wrongwrong/abifix/AMid.kt"
        const val ABI_TYPED = "consumer/src/main/kotlin/org/wrongwrong/abiuse/SweepTyped.kt"
    }

    // TC-XM-019: 下流モジュールでの非 final 末端サブタイプ追加は producer を dirty にしない
    // （サブタイプは階層外・entries 非参加のため、lib の生成物は再生成されずバイト不変）
    @Test
    fun downstreamSubtypeAdditionDoesNotDirtyProducer() {
        val dir = IcTestSupport.prepare("diag-cross-absorb", "swxm19-")
        TestKitHarness.build(dir, ":app:compileKotlin")
        val libDigests = IcTestSupport.classDigests(dir, "lib")

        TestKitHarness.writeFile(
            dir, ABSORB_APP_SQUARE,
            "package org.wrongwrong.diag.xabapp\n\nimport org.wrongwrong.diag.xab.XabSi\n\n" +
                "// TC-XM-019: 追加のサブタイプ（階層外。producer の entries / 生成物へ影響しない）\n" +
                "class Sq : XabSi.Poly()\n",
        )
        val second = TestKitHarness.build(dir, ":app:compileKotlin")

        assertEquals(TaskOutcome.UP_TO_DATE, second.task(":lib:compileKotlin")?.outcome)
        assertEquals(libDigests, IcTestSupport.classDigests(dir, "lib"))
    }

    // TC-XM-038: producer のコメントのみ編集（ABI 不変）では、共連れ再生成された producer の
    // 生成物が clean とバイト一致し、consumer の再コンパイルを誘発しない（跨モジュール決定性）
    @Test
    fun commentOnlyProducerEditKeepsBytesAndConsumerUpToDate() {
        val dir = IcTestSupport.prepare("abi-propagation", "swxm38-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain"))
        val producerDigests = IcTestSupport.classDigests(dir, "producer")

        TestKitHarness.replaceInFile(
            dir, ABI_SI,
            "// 跨モジュール ABI 伝播フィクスチャの基底（末端は別ファイル）",
            "// 跨モジュール ABI 伝播フィクスチャの基底（末端は別ファイル・sweep: コメントのみ編集）",
        )
        val second = TestKitHarness.build(dir, ":consumer:runMain")

        assertEquals(baseline, IcTestSupport.outLines(second))
        assertEquals(producerDigests, IcTestSupport.classDigests(dir, "producer"))
        assertEquals(TaskOutcome.UP_TO_DATE, second.task(":consumer:compileKotlin")?.outcome)
    }

    // TC-VIS-047 / TC-VIS-050: companion 可視性の #7-c 編集が asEnumish 返り値型の ABI 差分として
    // 跨モジュールへ伝播し（public→internal で規則 1→2 = consumer の具体型依存が壊れる）、
    // 逆編集（internal→public）で規則 1 へ復帰する
    @Test
    fun companionVisibilityTogglePropagatesReturnTypeAcrossModules() {
        val dir = IcTestSupport.prepare("abi-propagation", "swxm47-")
        TestKitHarness.writeFile(
            dir, ABI_TYPED,
            "package org.wrongwrong.abiuse\n\nimport org.wrongwrong.abifix.Foo\n\n" +
                "// asEnumish の返り値型（規則 1: 具体型 Foo.Companion）へ静的に依存する観測点（TC-VIS-047）\n" +
                "fun typedProbe(): String {\n" +
                "    val kind: Foo.Companion = Foo(3).asEnumish()\n" +
                "    return kind.label\n" +
                "}\n",
        )
        TestKitHarness.build(dir, ":consumer:compileKotlin")

        TestKitHarness.replaceInFile(
            dir, ABI_FOO,
            "class Foo(val v: Int) : SI",
            "class Foo(val v: Int) : SI {\n    internal companion object\n}",
        )
        val failure = TestKitHarness.buildAndFail(dir, ":consumer:compileKotlin")
        assertTrue(
            failure.output.lineSequence().any { it.contains("SweepTyped.kt:") && it.contains("e: ") },
            "規則 2 への切替が consumer の具体型依存を壊すこと:\n${failure.output}",
        )

        TestKitHarness.replaceInFile(
            dir, ABI_FOO,
            "class Foo(val v: Int) : SI {\n    internal companion object\n}",
            "class Foo(val v: Int) : SI",
        )
        val restored = TestKitHarness.build(dir, ":consumer:compileKotlin")
        assertTrue(restored.output.contains("BUILD SUCCESSFUL"), "逆編集で規則 1 へ復帰すること")
    }

    // TC-XM-055 / TC-GAP-016（IC 面）: 非入れ子の internal 中間 sealed の挿入で entries の並びが
    // 再配置され（[Bar,Foo]→[Foo,Bar]）、kind 集合は不変のため consumer の kind-when は壊れない
    @Test
    fun intermediateSealedInsertionReordersEntriesAcrossModules() {
        val dir = IcTestSupport.prepare("abi-propagation", "swxm55-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, ":consumer:runMain"))
        assertEquals(listOf("ENTRIES=Bar,Foo", "KINDS=bar,foo"), baseline)

        TestKitHarness.writeFile(
            dir, ABI_MID,
            "package org.wrongwrong.abifix\n\n" +
                "// 非入れ子の internal 中間 sealed（TC-XM-055 / TC-GAP-016。中間には何も生成されない）\n" +
                "internal sealed interface AMid : SI\n",
        )
        TestKitHarness.replaceInFile(dir, ABI_FOO, "class Foo(val v: Int) : SI", "class Foo(val v: Int) : AMid")
        val second = TestKitHarness.build(dir, ":consumer:runMain")

        // SI 継承者 [AMid, Bar] → AMid を [Foo] へ展開 → entries=[Foo, Bar]（末端集合 FQN 順ではない）
        assertEquals(listOf("ENTRIES=Foo,Bar", "KINDS=foo,bar"), IcTestSupport.outLines(second))
    }
}
