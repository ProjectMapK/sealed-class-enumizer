package io.github.projectmapk.gradle

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

// 跨 module ABI 伝播（docs/test/ケース06-ビルド動態.md BLD-34/35）:
// producer の sealed リスト変化が ABI 差分として未編集 consumer の再コンパイルを誘発し、
// else 無し kind-when が再検査されること（V1-b）と、その負値系（削除・@Enumize 除去）を検証する
class AbiPropagationTest {
    private val fixtureName = "abi-propagation"
    private val siFile = "producer/src/main/kotlin/io/github/projectmapk/abifix/Si.kt"
    private val fooFile = "producer/src/main/kotlin/io/github/projectmapk/abifix/Foo.kt"
    private val bazFile = "producer/src/main/kotlin/io/github/projectmapk/abifix/Baz.kt"
    private val useFile = "consumer/src/main/kotlin/io/github/projectmapk/abiuse/Use.kt"
    private val baselineOut = listOf("ENTRIES=Bar,Foo", "KINDS=bar,foo")

    // docs/test/ケース06-ビルド動態.md BLD-34: #11 末端追加で未編集 consumer が再コンパイルされ
    // 非網羅エラー → 枝追加で entries へ反映される
    @Test
    fun leafAdditionRecompilesConsumerAndReverifiesWhen() {
        val dir = IcTestSupport.prepare(fixtureName, "abi1-")
        val first = TestKitHarness.build(dir, ":consumer:runMain")
        assertEquals(baselineOut, IcTestSupport.outLines(first))

        // 基線: 編集なしの再ビルドでは producer / consumer とも UP-TO-DATE
        val noEdit = TestKitHarness.build(dir, ":consumer:compileKotlin")
        assertEquals(TaskOutcome.UP_TO_DATE, noEdit.task(":producer:compileKotlin")?.outcome)
        assertEquals(TaskOutcome.UP_TO_DATE, noEdit.task(":consumer:compileKotlin")?.outcome)

        TestKitHarness.writeFile(
            dir,
            bazFile,
            "package io.github.projectmapk.abifix\n\n// #11 で追加される末端（docs/test/ケース06-ビルド動態.md BLD-34）\ndata object Baz : SI\n",
        )
        // producer 側は基底不在ラウンド（新規ファイルでの末端追加）を IC 直行で通り、
        // 検証対象の consumer 側は未編集のまま IC で再検査される
        val failure = TestKitHarness.buildAndFail(dir, ":consumer:compileKotlin")
        assertEquals(TaskOutcome.SUCCESS, failure.task(":producer:compileKotlin")?.outcome)
        assertEquals(TaskOutcome.FAILED, failure.task(":consumer:compileKotlin")?.outcome)
        assertTrue(
            "exhaustive" in failure.output,
            "未編集 consumer の kind-when が再検査されること:\n${failure.output}",
        )

        TestKitHarness.replaceInFile(
            dir,
            useFile,
            "import io.github.projectmapk.abifix.Bar",
            "import io.github.projectmapk.abifix.Bar\nimport io.github.projectmapk.abifix.Baz",
        )
        TestKitHarness.replaceInFile(
            dir,
            useFile,
            "    Bar -> \"bar\"",
            "    Bar -> \"bar\"\n    Baz -> \"baz\"",
        )
        val fixed = TestKitHarness.build(dir, ":consumer:runMain")
        assertEquals(
            listOf("ENTRIES=Bar,Baz,Foo", "KINDS=bar,baz,foo"),
            IcTestSupport.outLines(fixed),
        )
    }

    // docs/test/ケース06-ビルド動態.md BLD-35: 末端削除 = consumer の名指し未解決・
    // @Enumize 除去 = Enumish 未解決 + producer の stale 掃除
    @Test
    fun leafDeletionAndEnumizeRemovalFailConsumer() {
        val dir = IcTestSupport.prepare(fixtureName, "abi2-")
        TestKitHarness.build(dir, ":consumer:compileKotlin")

        // 末端削除で、削除 kind を名指しする consumer がコンパイルエラーになる
        val fooContent = IcTestSupport.readFile(dir, fooFile)
        TestKitHarness.deleteFile(dir, fooFile)
        val deletion = TestKitHarness.buildAndFail(dir, ":consumer:compileKotlin")
        assertTrue(
            "Unresolved reference" in deletion.output && "Foo" in deletion.output,
            "削除 kind の名指しが未解決になること:\n${deletion.output}",
        )
        // 復元は「末端を含む新規ファイルの追加」= 基底不在ラウンドであり、IC 直行で成立する
        TestKitHarness.writeFile(dir, fooFile, fooContent)
        TestKitHarness.build(dir, ":consumer:compileKotlin")

        // @Enumize 除去で生成 API が消え、consumer の参照が未解決・producer の stale が掃除される
        TestKitHarness.replaceInFile(
            dir,
            siFile,
            "@Enumize\nsealed interface SI",
            "sealed interface SI",
        )
        val removal = TestKitHarness.buildAndFail(dir, ":consumer:compileKotlin")
        assertEquals(TaskOutcome.SUCCESS, removal.task(":producer:compileKotlin")?.outcome)
        assertTrue(
            "Unresolved reference" in removal.output && "Enumish" in removal.output,
            "生成 API の消失が未解決参照になること:\n${removal.output}",
        )
        val producerKeys = IcTestSupport.classDigests(dir, "producer").keys
        assertTrue(
            producerKeys.none { it.startsWith("io/github/projectmapk/abifix/SI\$") },
            "生成物の stale 掃除: $producerKeys",
        )
    }
}
