package org.wrongwrong.gradle

import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

// 参照不能 kind 用 IR-only アクセサの IC 決定性（概要 §8・設計02 §4.3・docs/テストケース管理.md）:
// createEntries はトップレベルアクセサ（KaHidden.Leaf）とネストアクセサ（KaPriv の private companion）を
// 呼ぶ。clean / incremental / from-cache で生成物のバイトと entries が決定的であること、末端ファイル単独の
// ABI 非変更編集で基底帰属の生成物（アクセサ呼び出しを含む createEntries）が安定であることを検証する
class IcKindAccessorTest {
    private val fixtureName = "ic-kind-accessor"
    private val hiddenFile = "src/main/kotlin/org/wrongwrong/ickacc/KaHidden.kt"

    // 基底帰属の生成物（KaSi$Enumish / その Companion / $EntriesHolder）を指す接頭辞
    private val generatedPrefix = "org/wrongwrong/ickacc/KaSi\$Enumish"

    private val expectedOut = listOf("ENTRIES=Leaf,KaPriv,Visible", "VALUEOF=Leaf,KaPriv")

    @Test
    fun cleanIncrementalAndFromCacheProduceIdenticalOutputs() {
        val dir = IcTestSupport.prepare(fixtureName, "ickacc1-")
        val first = TestKitHarness.build(dir, "runMain")
        assertEquals(expectedOut, IcTestSupport.outLines(first))
        val digests0 = IcTestSupport.classDigests(dir)

        // 無編集の再ビルド: コンパイルは走らず、アクセサを含む生成物のバイトが一致
        val second = TestKitHarness.build(dir, "runMain")
        assertEquals(TaskOutcome.UP_TO_DATE, second.task(":compileKotlin")?.outcome)
        assertEquals(expectedOut, IcTestSupport.outLines(second))
        assertEquals(digests0, IcTestSupport.classDigests(dir))

        // clean 後の再ビルド: build cache から復元され、復元物がバイト一致
        TestKitHarness.build(dir, "clean")
        val third = TestKitHarness.build(dir, "runMain")
        assertEquals(TaskOutcome.FROM_CACHE, third.task(":compileKotlin")?.outcome)
        assertEquals(expectedOut, IcTestSupport.outLines(third))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // 末端ファイル単独の ABI 非変更編集（コメント変更）→ 階層共連れで再コンパイルされても、基底帰属の
    // 生成物（アクセサ呼び出しを組む createEntries）はバイト一致し、entries も不変（P1・アクセサの決定性）
    @Test
    fun leafFileEditKeepsAccessorBasedEntriesStable() {
        val dir = IcTestSupport.prepare(fixtureName, "ickacc2-")
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val siGenerated0 = IcTestSupport.classDigests(dir).filterKeys { it.startsWith(generatedPrefix) }

        TestKitHarness.replaceInFile(
            dir, hiddenFile,
            "IR-only アクセサ経由で load",
            "IR-only アクセサ経由で取得",
        )
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(siGenerated0, IcTestSupport.classDigests(dir).filterKeys { it.startsWith(generatedPrefix) })
    }
}
