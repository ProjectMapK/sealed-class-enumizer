package org.wrongwrong.gradle

import kotlin.test.Test
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticInFile
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsentAt

// 跨 module の負値診断（docs/test/ケース04-診断.md DIA-18/21/22/57）。
// diag-cross フィクスチャ（lib + app = 適用消費 + app2 = 未適用消費）を --continue の
// 1 buildAndFail で束ね、適用側と未適用側の発火差をまとめて観測する
class DiagCrossTest : DiagTestBase() {
    private fun fail(): String = failOutput("diag-cross", "compileKotlin", "--continue")

    // docs/test/ケース04-診断.md DIA-18: 別 module の 2 末端実装は利用側（適用）module の宣言へ AK 報告
    @Test
    fun crossModuleMultiLeafImplementationIsAmbiguous() {
        assertDiagnosticAt(fail(), "Cross.kt", 7, DiagFragments.AMBIGUOUS_KIND, "LeafA", "LeafB")
    }

    // docs/test/ケース04-診断.md DIA-21: 未適用利用側の 2 末端実装は言語 MANY_IMPL_MEMBER のみ・AK 不在
    @Test
    fun withoutPluginOnlyLanguageErrorIsReported() {
        val output = fail()
        assertDiagnosticInFile(output, "Cross3.kt", DiagFragments.LANG_MANY_IMPL_MEMBER)
        assertFragmentAbsentAt(output, "Cross3.kt", DiagFragments.AMBIGUOUS_KIND)
    }

    // docs/test/ケース04-診断.md DIA-22: 2 階層の末端 interface 実装は言語 INCONSISTENT_TYPE_ARGS のみ
    // （Enumized の型引数不一致で言語が塞ぎ MH / AK 不在）
    @Test
    fun twoHierarchyImplementationFailsWithLanguageErrorOnly() {
        val output = fail()
        assertDiagnosticInFile(output, "Cross2.kt", DiagFragments.LANG_INCONSISTENT_TYPE_ARGS)
        assertFragmentAbsentAt(output, "Cross2.kt", DiagFragments.MULTIPLE_HIERARCHIES)
        assertFragmentAbsentAt(output, "Cross2.kt", DiagFragments.AMBIGUOUS_KIND)
    }

    // docs/test/ケース04-診断.md DIA-57: 別 module の Rogue は言語 sealed 制約と MIOH の併発（適用側）
    @Test
    fun crossModuleRogueFailsWithSealedRuleAndManualImpl() {
        val output = fail()
        assertDiagnosticInFile(output, "Rogue.kt", DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY)
        assertDiagnosticInFile(output, "Rogue.kt", DiagFragments.LANG_SEALED_DIFFERENT_MODULE)
    }
}
