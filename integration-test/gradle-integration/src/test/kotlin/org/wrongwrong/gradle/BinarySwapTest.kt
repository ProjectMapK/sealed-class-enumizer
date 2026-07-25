package org.wrongwrong.gradle

import kotlin.io.path.readBytes
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

// 旧バイナリ差し替え（docs/概要.md §7・docs/テストケース管理.md TC-XM-023/024/025/053）:
// v1（2 末端）でコンパイルした consumer を、実行時のみ v2（3 末端）の jar と組み合わせると、
// entries は「実行時に存在する版の集合」を返す（entries アクセサ非 inline の帰結）。
// v1 網羅の else 無し kind-when へ実行時の未知 kind を通すと NoWhenBranchMatchedException になる
class BinarySwapTest {
    @Test
    fun consumerCompiledAgainstV1SeesV2EntriesAtRuntime() {
        val dir = IcTestSupport.prepare("binary-swap", "swap-")

        // consumer は files() 依存のため、先に両版の jar を組み立ててからコンパイルする
        TestKitHarness.build(dir, ":producer-v1:jar", ":producer-v2:jar")
        TestKitHarness.build(dir, ":consumer:compileKotlin")

        // v1 実行: コンパイル時と同じ 2 末端。valueOf("Baz") は失敗し、kind-when は全 kind を網羅
        val runV1 = TestKitHarness.build(dir, ":consumer:runV1")
        assertEquals(
            listOf("COUNT=2", "ENTRIES=Bar,Foo", "PROBE_BAZ=IAE", "WHEN=Bar->bar,Foo->foo"),
            IcTestSupport.outLines(runV1),
        )

        // v2 差し替え実行: consumer は再コンパイルされないまま、entries が実行時の 3 末端を返し
        // （TC-XM-023）、valueOf("Baz") が解決され（TC-XM-025）、v1 網羅の when は Baz kind で
        // NoWhenBranchMatchedException になる（TC-XM-053）
        val runV2 = TestKitHarness.build(dir, ":consumer:runV2")
        assertEquals(TaskOutcome.UP_TO_DATE, runV2.task(":consumer:compileKotlin")?.outcome)
        assertEquals(
            listOf(
                "COUNT=3",
                "ENTRIES=Bar,Baz,Foo",
                "PROBE_BAZ=Baz",
                "WHEN=Bar->bar,Baz->NoWhenBranchMatchedException,Foo->foo",
            ),
            IcTestSupport.outLines(runV2),
        )

        // TC-XM-024: entries アクセサが inline されず、getEntries のメンバー呼び出しが
        // consumer の定数プールに残っていること（inline なら v1 の集合が焼き込まれ差し替えが壊れる）
        val mainClass =
            dir.resolve("consumer/build/classes/kotlin/main/org/wrongwrong/swapuse/MainKt.class")
        val constantPoolText = String(mainClass.readBytes(), Charsets.ISO_8859_1)
        assertTrue("getEntries" in constantPoolText, "entries アクセサ呼び出しがメンバー参照のまま残ること")
    }
}
