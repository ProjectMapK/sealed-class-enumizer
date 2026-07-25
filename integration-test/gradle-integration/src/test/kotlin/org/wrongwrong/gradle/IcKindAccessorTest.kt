package org.wrongwrong.gradle

import kotlin.test.assertEquals
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

// 参照不能 kind 用 IR-only アクセサの IC 決定性（docs/概要.md §8・docs/コンパイラプラグイン設計02.md §4.3・docs/テストケース管理.md）:
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
        val siGenerated0 =
            IcTestSupport.classDigests(dir).filterKeys { it.startsWith(generatedPrefix) }

        TestKitHarness.replaceInFile(dir, hiddenFile, "IR-only アクセサ経由で load", "IR-only アクセサ経由で取得")
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(
            siGenerated0,
            IcTestSupport.classDigests(dir).filterKeys { it.startsWith(generatedPrefix) },
        )
    }

    // 新規ファイルでの末端追加（基底不在ラウンド）を、参照不能 kind を含む階層で行う。1 巡目は基底不在、
    // 2 巡目で基底が再コンパイルされてアクセサ呼び出しを含む createEntries が組み直される（docs/コンパイラプラグイン設計00.md §5.1）。
    // アクセサ生成は基底側の生成に属するため、この収束が成立することを IC 直行で固定する
    @Test
    fun newLeafFileRebuildsAccessorBasedEntries() {
        val dir = IcTestSupport.prepare(fixtureName, "ickacc3-")
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))

        TestKitHarness.writeFile(
            dir,
            "src/main/kotlin/org/wrongwrong/ickacc/KaNew.kt",
            "package org.wrongwrong.ickacc\n\n// 基底不在ラウンドの引き金となる新規末端\ndata object KaNew : KaSi\n",
        )
        val added = listOf("ENTRIES=Leaf,KaNew,KaPriv,Visible", "VALUEOF=Leaf,KaPriv")
        assertEquals(added, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val incremental = IcTestSupport.classDigests(dir)
        assertEquals(added, IcTestSupport.outLines(TestKitHarness.build(dir, "clean", "runMain")))
        assertEquals(IcTestSupport.classDigests(dir), incremental, "アクセサを含む生成物が clean と一致すること")
    }
}
