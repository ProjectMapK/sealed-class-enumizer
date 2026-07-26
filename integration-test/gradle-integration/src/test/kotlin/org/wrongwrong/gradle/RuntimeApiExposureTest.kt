package org.wrongwrong.gradle

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

// runtime-api 依存露出（docs/test/ケース06-ビルド動態.md BLD-41・docs/test/ケース05-境界横断.md
// XMP-23/24）。gradle-plugin が runtime-api を api スコープで自動追加するため、未適用の消費側は
// 明示宣言なしで生成 API（supertype = runtime-api の Enumish / Enumized）を推移解決できる。
// 自動追加のオプトアウト + implementation 隠しの縮退では supertype 解決に失敗する（正値 / 縮退の対）
class RuntimeApiExposureTest {
    private val fixture = "runtime-api-exposure"

    // docs/test/ケース05-境界横断.md XMP-23: api 自動追加により未適用 consumer が推移解決できる
    @Test
    fun consumerResolvesViaTransitiveApi() {
        val dir = IcTestSupport.prepare(fixture, "exposeApi-")
        val result = TestKitHarness.build(dir, ":consumer:compileKotlin")
        assertEquals(TaskOutcome.SUCCESS, result.task(":consumer:compileKotlin")?.outcome)
    }

    // docs/test/ケース05-境界横断.md XMP-24: addRuntimeDependency=false + implementation 隠しでは
    // producer は成功・consumer は supertype Enumish 未解決で失敗する
    @Test
    fun consumerFailsWhenRuntimeApiHidden() {
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
