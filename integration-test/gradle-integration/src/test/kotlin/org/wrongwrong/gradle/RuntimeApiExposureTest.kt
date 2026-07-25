package org.wrongwrong.gradle

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

// runtime-api 依存露出（docs/テストケース管理.md TC-XM-006 / TC-XM-056）。
// gradle-plugin が runtime-api を api スコープで自動追加するため、プラグイン未適用の消費側は
// runtime-api を明示宣言しなくても生成 API（supertype = runtime-api の Enumish / Enumized）を解決できる。
// 併せて、自動追加をオプトアウトし implementation で「隠した」縮退では消費側が supertype 解決に失敗すること
// （api 公開が必須であること = runtime-api を api 公開しない場合の NG 形）を 1 対で観測する。
// この対比は api 公開が既定になって初めて成立する（api 公開が既定になる前は正側が構成できなかった）。
class RuntimeApiExposureTest {
    private val fixture = "runtime-api-exposure"

    // TC-XM-006 正 / TC-XM-056: producer の api 公開により、未適用消費側が明示宣言なしで生成 API を解決できる
    @Test
    fun consumerResolvesGeneratedApiViaTransitiveApi() {
        val dir = IcTestSupport.prepare(fixture, "exposeApi-")
        val result = TestKitHarness.build(dir, ":consumer:compileKotlin")
        assertEquals(TaskOutcome.SUCCESS, result.task(":consumer:compileKotlin")?.outcome)
    }

    // TC-XM-006 縮退 / TC-XM-056 オプトアウト: 自動追加を無効化し runtime-api を implementation で隠すと、
    // producer はコンパイルできる一方、消費側は生成 API の supertype（Enumish）を解決できず失敗する
    @Test
    fun consumerFailsWhenRuntimeApiIsHiddenAsImplementation() {
        val dir = IcTestSupport.prepare(fixture, "hideApi-")
        val result = TestKitHarness.buildAndFail(dir, ":consumer:compileKotlin", "-PhideRuntimeApi")
        assertEquals(TaskOutcome.SUCCESS, result.task(":producer:compileKotlin")?.outcome)
        assertEquals(TaskOutcome.FAILED, result.task(":consumer:compileKotlin")?.outcome)
        assertTrue(
            "Enumish" in result.output && "supertype" in result.output,
            "runtime-api を隠すと supertype Enumish が未解決になること:\n${result.output}",
        )
    }
}
