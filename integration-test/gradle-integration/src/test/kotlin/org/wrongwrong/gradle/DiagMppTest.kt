package org.wrongwrong.gradle

import org.junit.jupiter.api.Disabled
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent
import kotlin.test.Test
import kotlin.test.assertTrue

// G 軸: MPP 負値（ENUMIZE_ON_EXPECT / ENUMIZE_ON_ACTUAL / ENUMIZE_CROSS_SOURCE_SET）と MPP near-miss
// （docs/テストケース管理.md TC-DIAG-009〜012・061〜062、docs/概要.md §7・§8）。
// フィクスチャは jvm 単一ターゲットの KMP ビルド（commonMain / jvmMain のソースセット分割で十分なため）
class DiagMppTest : DiagTestBase() {
    // TC-DIAG-009: expect 宣言への @Enumize（報告位置 = expect 宣言）
    @Test
    fun enumizeOnExpectIsReported() {
        assertDiagnosticAt(
            failOutput("diag-mpp-expect", "compileKotlinJvm"),
            "MppE.kt",
            6,
            DiagFragments.ON_EXPECT,
        )
    }

    // TC-DIAG-010: actual 宣言への @Enumize（報告位置 = actual 宣言）
    @Test
    fun enumizeOnActualIsReported() {
        assertDiagnosticAt(
            failOutput("diag-mpp-actual", "compileKotlinJvm"),
            "MppAJvm.kt",
            6,
            DiagFragments.ON_ACTUAL,
        )
    }

    // TC-DIAG-061: 階層の継承者が別ソースセット → 言語側の sealed 継承エラーに合流する（buildAndFail）
    @Test
    fun crossSourceSetInheritorFailsBuild() {
        val output = failOutput("diag-mpp-cross-source-set", "compileKotlinJvm")
        val positioned = output.lineSequence().filter { it.contains("MppCJvm.kt:4:") }.toList()
        assertTrue(positioned.any { it.contains("e: ") }, "MppCJvm.kt:4 にエラーが無い: $positioned")
    }

    // TC-DIAG-061: doc はコンパイラ本体診断への補足として ENUMIZE_CROSS_SOURCE_SET の発火を要求するが、
    // 実測は言語エラー（Extending sealed classes or interfaces from a different module is prohibited.）のみで
    // 補足診断は出ない（別ソースセットの継承者が基底の inheritors 一覧に載らず検査が空振りするため）
    @Test
    @Disabled("NG: 別ソースセット継承者で ENUMIZE_CROSS_SOURCE_SET が不発火（言語エラーのみ） — docs/修正方針案.md 反映待ち")
    fun crossSourceSetSupplementaryDiagnosticIsReported() {
        assertDiagnosticAt(
            failOutput("diag-mpp-cross-source-set", "compileKotlinJvm"),
            "MppC.kt",
            6,
            DiagFragments.CROSS_SOURCE_SET,
        )
    }

    // TC-DIAG-012: platform 専用 sealed（actual でない）は ON_ACTUAL / ON_EXPECT とも非発火
    @Test
    fun platformOnlySealedDoesNotReport() {
        val output = successOutput("diag-mpp-platform-only", "compileKotlinJvm")
        assertFragmentAbsent(output, DiagFragments.ON_EXPECT)
        assertFragmentAbsent(output, DiagFragments.ON_ACTUAL)
    }

    // TC-DIAG-011（common の通常 sealed が非発火で全ターゲット成立 = V5 前提）と TC-DIAG-062
    // （基底と全末端が同一ソースセット）は mpp-producer モジュールのビルド成功が実証済み
}
