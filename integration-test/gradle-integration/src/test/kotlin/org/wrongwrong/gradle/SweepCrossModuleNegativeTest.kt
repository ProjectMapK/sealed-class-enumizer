package org.wrongwrong.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// 残ケース掃討: 跨モジュール可視性の負値（sweep-xm-negative フィクスチャ = producer + consumer）。
// producer は正常にコンパイルされ、consumer 側の参照だけが言語の可視性・網羅性エラーで壊れることを
// 1 回の buildAndFail で観測する（docs/エッジケースへの対応方針.md §1.2・設計00 §5.2）
class SweepCrossModuleNegativeTest : DiagTestBase() {
    private var cached: BuildResult? = null

    private fun result(): BuildResult {
        val current = cached
        if (current != null) {
            return current
        }
        val built = TestKitHarness.buildAndFail(prepare("sweep-xm-negative"), ":consumer:compileKotlin")
        cached = built
        return built
    }

    private fun assertErrorInFile(file: String, vararg fragments: String) {
        val lines = result().output.lineSequence()
            .filter { it.contains("$file:") && it.contains("e: ") }
            .toList()
        assertTrue(
            lines.any { candidate -> fragments.all(candidate::contains) },
            "期待したエラーが $file に無い: ${fragments.toList()}\n$file の診断行: $lines",
        )
    }

    // 可視性混在の 3 階層を持つ producer 自体は正常にコンパイルされる（negatives は消費側のみ）
    @Test
    fun producerCompilesCleanly() {
        assertEquals(TaskOutcome.SUCCESS, result().task(":producer:compileKotlin")?.outcome)
    }

    // TC-VIS-004 / TC-XM-051: internal 一色の階層は別モジュールから基底・生成 API とも参照不能
    @Test
    fun internalHierarchyIsInvisibleAcrossModule() {
        assertErrorInFile("UseInternal.kt", "SwXnInt")
    }

    // TC-XM-010 負値側 / TC-VIS-036: 外側から internal kind を名指しできず else 無し when は網羅不成立
    @Test
    fun elseIsRequiredWhereInternalKindIsInvisible() {
        assertErrorInFile("UseElse.kt", "exhaustive")
    }

    // TC-VIS-038: 基底より広い末端でも sealed 親（内包する生成 Enumish）が internal で不可視の
    // 位置では、階層単位の kind 網羅 when を構成できない（sealed 親の名指しが可視性エラー）。
    // なお規則 1 の具体型（companion object 型）に閉じた単一 kind の when は object 型の網羅として
    // else 無しで成立する（実測。フィクスチャ UseWide.kt の useWideSingle が固定する）
    @Test
    fun widerLeafHierarchyWhenCannotBeFormedWhereBaseIsInvisible() {
        assertErrorInFile("UseWide.kt", "SwXnWideBase")
    }
}
