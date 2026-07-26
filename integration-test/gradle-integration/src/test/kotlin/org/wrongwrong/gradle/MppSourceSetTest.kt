package org.wrongwrong.gradle

import kotlin.test.Test
import kotlin.test.assertTrue
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAnywhere

// MPP のソースセット境界（docs/test/ケース04-診断.md DIA-11/12）。
// HMPP 派生ソースセットへの末端逸脱の言語委譲と、common 診断の metadata コンパイル単独発火を検証する
class MppSourceSetTest : DiagTestBase() {
    // docs/test/ケース04-診断.md DIA-11: 中間ソースセット（webMain）基底 × 派生ソースセット（jsMain）
    // 末端は言語エラー（sealed は「同一ソースセット」を要求し「可視な派生ソースセット」では不足する）
    @Test
    fun hmppDerivedSourceSetLeafFailsBuild() {
        val output = failOutput("sweep-mpp-hmpp", "compileKotlinJs")
        val positioned =
            output
                .lineSequence()
                .filter { it.contains("SwHmppJsLeaf.kt:") && it.contains("e: ") }
                .toList()
        assertTrue(positioned.isNotEmpty(), "派生ソースセットの末端にエラーが出ること:\n$output")
    }

    // docs/test/ケース04-診断.md DIA-12: metadata コンパイル単独で LABEL_CLASH が発火する
    // （チェッカーは MppCheckerKind.Common）
    @Test
    fun labelClashFiresInMetadataCompilation() {
        val output = failOutput("sweep-mpp-metadata-diag", "compileCommonMainKotlinMetadata")
        assertDiagnosticAnywhere(output, DiagFragments.LABEL_CLASH)
        assertDiagnosticAnywhere(output, "Dup")
    }
}
