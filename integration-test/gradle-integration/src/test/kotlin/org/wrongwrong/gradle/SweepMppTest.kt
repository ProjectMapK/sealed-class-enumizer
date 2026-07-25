package org.wrongwrong.gradle

import kotlin.test.Test
import kotlin.test.assertTrue
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAnywhere
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent

// 残ケース掃討: MPP のソースセット境界（sweep-mpp-* フィクスチャ）。
// expect/actual 末端の near-miss・HMPP 派生ソースセットへの逸脱・common 診断の metadata 発火を検証する
class SweepMppTest : DiagTestBase() {
    // TC-MPP-049: 末端が expect/actual class（基底は common の非 expect）。@Enumize の付与先は
    // 通常宣言のため ON_EXPECT / ON_ACTUAL は発火せず、actual が platform ソースセットへ実現される
    // 構成は sealed のソースセット制約系の言語エラーへ合流する（実測固定）
    @Test
    fun expectActualLeafFailsWithoutOnExpectOnActual() {
        val output = failOutput("sweep-mpp-expect-leaf", "compileKotlinJvm")
        assertFragmentAbsent(output, DiagFragments.ON_EXPECT)
        assertFragmentAbsent(output, DiagFragments.ON_ACTUAL)
        assertTrue(output.lineSequence().any { it.contains("e: ") }, "言語側エラーへ合流すること:\n$output")
    }

    // TC-MPP-051: 中間ソースセット（webMain）の基底 × 派生ソースセット（jsMain）の末端。
    // sealed は「同一ソースセット」を要求し「可視な派生ソースセット」では不足するため言語エラー。
    // common↔platform（TC-DIAG-061）と同様、プラグイン側の補足診断は持たない（設計01 §7.2）
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

    // TC-MPP-065: commonMain の label 衝突は metadata コンパイル（:compileCommonMainKotlinMetadata）
    // 単独で発火する（FIR 診断が platform コンパイルを待たずに common で観測される）
    @Test
    fun labelClashFiresInMetadataCompilation() {
        val output = failOutput("sweep-mpp-metadata-diag", "compileCommonMainKotlinMetadata")
        assertDiagnosticAnywhere(output, DiagFragments.LABEL_CLASH)
        assertDiagnosticAnywhere(output, "Dup")
    }
}
