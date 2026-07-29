package io.github.projectmapk.gradle

import io.github.projectmapk.gradle.DiagAsserts.assertDiagnosticAt
import io.github.projectmapk.gradle.DiagAsserts.assertFragmentAbsent
import io.github.projectmapk.gradle.DiagAsserts.assertFragmentAbsentAt
import kotlin.test.Test

// MPP 境界の診断（docs/test/ケース04-診断.md DIA-06〜10）。
// diag-mpp フィクスチャは jvm 単一ターゲット KMP の fail モジュール（buildAndFail）と
// ok モジュール（成功ビルド）を持ち、DiagTestBase の複数ビルドキャッシュで各 1 回だけ実行する
class DiagMppTest : DiagTestBase() {
    private fun fail(): String = failOutput("diag-mpp", ":fail:compileKotlinJvm")

    private fun ok(): String = successOutput("diag-mpp", ":ok:compileKotlinJvm")

    // docs/test/ケース04-診断.md DIA-06: expect 宣言への @Enumize → ENUMIZE_ON_EXPECT
    @Test
    fun enumizeOnExpectIsReported() {
        assertDiagnosticAt(fail(), "MppE.kt", 6, DiagFragments.ON_EXPECT)
    }

    // docs/test/ケース04-診断.md DIA-06: 非 sealed × expect は NS 単独
    // （checkBase の早期リターンで ON_EXPECT 不在 = 抑止範囲の固定）
    @Test
    fun notSealedExpectReportsNsOnly() {
        val output = fail()
        assertDiagnosticAt(output, "MppNs.kt", 7, DiagFragments.NOT_SEALED)
        assertFragmentAbsentAt(output, "MppNs.kt", DiagFragments.ON_EXPECT)
    }

    // docs/test/ケース04-診断.md DIA-07: actual 宣言への @Enumize → ENUMIZE_ON_ACTUAL 単独
    @Test
    fun enumizeOnActualIsReported() {
        val output = fail()
        assertDiagnosticAt(output, "MppAJvm.kt", 6, DiagFragments.ON_ACTUAL)
        assertFragmentAbsentAt(output, "MppAJvm.kt", DiagFragments.ON_EXPECT)
    }

    // docs/test/ケース04-診断.md DIA-08: 非 expect/actual の sealed（common 完結・platform 専用）は非発火
    @Test
    fun nonExpectActualSealedDoesNotReport() {
        val output = ok()
        assertFragmentAbsent(output, DiagFragments.ON_EXPECT)
        assertFragmentAbsent(output, DiagFragments.ON_ACTUAL)
    }

    // docs/test/ケース04-診断.md DIA-09: expect/actual 末端は ON_EXPECT / ON_ACTUAL 対象外。
    // 言語の sealed 制約エラー（different module）は actual 宣言側に出て expect 側は無診断
    @Test
    fun expectActualLeafMergesIntoLanguageError() {
        val output = fail()
        assertFragmentAbsentAt(output, "MelLeaf.kt", "e: ")
        assertFragmentAbsentAt(output, "MelLeaf.kt", DiagFragments.ON_EXPECT)
        assertDiagnosticAt(output, "MelLeafJvm.kt", 4, DiagFragments.LANG_SEALED_DIFFERENT_MODULE)
        assertFragmentAbsentAt(output, "MelLeafJvm.kt", DiagFragments.ON_ACTUAL)
    }

    // docs/test/ケース04-診断.md DIA-10: common 基底 × platform 末端は言語 sealed 制約エラーのみ
    @Test
    fun crossSourceSetLeafFailsWithLanguageErrorOnly() {
        val output = fail()
        assertDiagnosticAt(output, "MppCJvm.kt", 5, DiagFragments.LANG_SEALED_DIFFERENT_MODULE)
        assertFragmentAbsentAt(output, "MppCJvm.kt", DiagFragments.NESTED_IN_HIERARCHY)
    }
}
