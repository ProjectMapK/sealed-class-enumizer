package org.wrongwrong.gradle

import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// 跨モジュール ABI 伝播（設計00 §5.3 #11・概要 §7・docs/テストケース管理.md TC-XM-013〜015・
// TC-IC-027/044/046/056・TC-XM-044）: producer の sealed リスト変化が ABI 差分として未編集の
// consumer の再コンパイルを誘発し、else 無し kind-when が再検査されること（V1-b）を検証する
class AbiPropagationTest {
    private val fixtureName = "abi-propagation"
    private val siFile = "producer/src/main/kotlin/org/wrongwrong/abifix/Si.kt"
    private val fooFile = "producer/src/main/kotlin/org/wrongwrong/abifix/Foo.kt"
    private val bazFile = "producer/src/main/kotlin/org/wrongwrong/abifix/Baz.kt"
    private val useFile = "consumer/src/main/kotlin/org/wrongwrong/abiuse/Use.kt"
    private val rogueFile = "consumer/src/main/kotlin/org/wrongwrong/abiuse/Rogue.kt"
    private val baselineOut = listOf("ENTRIES=Bar,Foo", "KINDS=bar,foo")

    // #11 末端追加（TC-XM-013 = TC-IC-027・TC-IC-044）: 未編集の consumer が UP-TO-DATE にならず
    // 再コンパイルされ、else 無し kind-when が網羅性エラーになる（V1-b）。枝追加で復帰し、
    // 実行時 entries へ新末端が反映される（TC-XM-015）
    @Test
    fun leafAdditionRecompilesConsumerAndFailsNonExhaustiveWhen() {
        val dir = IcTestSupport.prepare(fixtureName, "abi1-")
        val first = TestKitHarness.build(dir, ":consumer:runMain")
        assertEquals(baselineOut, IcTestSupport.outLines(first))

        // 基線: 編集なしの再ビルドでは producer / consumer とも UP-TO-DATE
        val noEdit = TestKitHarness.build(dir, ":consumer:compileKotlin")
        assertEquals(TaskOutcome.UP_TO_DATE, noEdit.task(":producer:compileKotlin")?.outcome)
        assertEquals(TaskOutcome.UP_TO_DATE, noEdit.task(":consumer:compileKotlin")?.outcome)

        TestKitHarness.writeFile(
            dir, bazFile,
            "package org.wrongwrong.abifix\n\n// #11 で追加される末端（docs/テストケース管理.md TC-XM-013）\ndata object Baz : SI\n",
        )
        // producer 側の IC 直行は「末端の新規ファイル追加」で既知の ICE になる（基底不在ラウンド。
        // 再現ゲートは Kt86121Test・NG 記録は IcRegressionTest の TC-IC-011）ため、producer のみ
        // clean で迂回する。検証対象の consumer 側は未編集のまま IC で再検査される（#11 の本質は不変）
        TestKitHarness.build(dir, ":producer:clean")
        val failure = TestKitHarness.buildAndFail(dir, ":consumer:compileKotlin")
        assertEquals(TaskOutcome.SUCCESS, failure.task(":producer:compileKotlin")?.outcome)
        assertEquals(TaskOutcome.FAILED, failure.task(":consumer:compileKotlin")?.outcome)
        assertTrue("exhaustive" in failure.output, "未編集 consumer の kind-when が再検査されること:\n${failure.output}")

        TestKitHarness.replaceInFile(
            dir, useFile,
            "import org.wrongwrong.abifix.Bar",
            "import org.wrongwrong.abifix.Bar\nimport org.wrongwrong.abifix.Baz",
        )
        TestKitHarness.replaceInFile(dir, useFile, "    Bar -> \"bar\"", "    Bar -> \"bar\"\n    Baz -> \"baz\"")
        val fixed = TestKitHarness.build(dir, ":consumer:runMain")
        assertEquals(listOf("ENTRIES=Bar,Baz,Foo", "KINDS=bar,baz,foo"), IcTestSupport.outLines(fixed))
    }

    // 末端削除（TC-XM-014 = TC-IC-046）・@Enumize 除去（TC-IC-056）・跨モジュール手動実装
    // （TC-XM-044: 生成 Enumish は sealed のため別モジュールから実装できない）の負値系
    @Test
    fun leafDeletionEnumizeRemovalAndCrossModuleManualImplFail() {
        val dir = IcTestSupport.prepare(fixtureName, "abi2-")
        TestKitHarness.build(dir, ":consumer:compileKotlin")

        // TC-XM-044: 別モジュールの object Rogue : SI.Enumish は sealed の言語制約でコンパイル不能
        TestKitHarness.writeFile(
            dir, rogueFile,
            "package org.wrongwrong.abiuse\n\nimport kotlin.reflect.KClass\nimport org.wrongwrong.abifix.SI\n\n" +
                "// 跨モジュールの手動実装（sealed 制約により不可 = docs/テストケース管理.md TC-XM-044）\n" +
                "object Rogue : SI.Enumish {\n" +
                "    override val label: String get() = \"Rogue\"\n\n" +
                "    override val enumizedClass: KClass<out SI> get() = SI::class\n" +
                "}\n",
        )
        val sealedViolation = TestKitHarness.buildAndFail(dir, ":consumer:compileKotlin")
        assertTrue("Rogue.kt" in sealedViolation.output, "跨モジュール手動実装の拒否:\n${sealedViolation.output}")
        TestKitHarness.deleteFile(dir, rogueFile)

        // TC-XM-014: 末端削除で、削除 kind を名指しする consumer がコンパイルエラーになる
        val fooContent = IcTestSupport.readFile(dir, fooFile)
        TestKitHarness.deleteFile(dir, fooFile)
        val deletion = TestKitHarness.buildAndFail(dir, ":consumer:compileKotlin")
        assertTrue(
            "Unresolved reference" in deletion.output && "Foo" in deletion.output,
            "削除 kind の名指しが未解決になること:\n${deletion.output}",
        )
        // 復元は「末端を含む新規ファイルの追加」になり IC 直行では既知の ICE（基底不在ラウンド）を踏むため、
        // producer のみ clean で迂回して復帰させる
        TestKitHarness.writeFile(dir, fooFile, fooContent)
        TestKitHarness.build(dir, ":producer:clean")
        TestKitHarness.build(dir, ":consumer:compileKotlin")

        // TC-IC-056: @Enumize 除去で生成 API が消え、consumer の参照が未解決・producer の stale が掃除される
        TestKitHarness.replaceInFile(dir, siFile, "@Enumize\nsealed interface SI", "sealed interface SI")
        val removal = TestKitHarness.buildAndFail(dir, ":consumer:compileKotlin")
        assertEquals(TaskOutcome.SUCCESS, removal.task(":producer:compileKotlin")?.outcome)
        assertTrue(
            "Unresolved reference" in removal.output && "Enumish" in removal.output,
            "生成 API の消失が未解決参照になること:\n${removal.output}",
        )
        val producerKeys = IcTestSupport.classDigests(dir, "producer").keys
        assertTrue(producerKeys.none { it.startsWith("org/wrongwrong/abifix/SI\$") }, "生成物の stale 掃除: $producerKeys")
    }
}
