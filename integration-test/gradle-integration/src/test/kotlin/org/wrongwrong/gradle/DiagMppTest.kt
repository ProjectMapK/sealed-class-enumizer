package org.wrongwrong.gradle

import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent
import kotlin.test.Test

// G 軸: MPP 負値（ENUMIZE_ON_EXPECT / ENUMIZE_ON_ACTUAL / 別ソースセット継承者の言語エラー）と MPP near-miss
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

    // TC-DIAG-061: 階層の継承者が別ソースセット → コンパイラ本体の sealed 制約エラーへ合流する
    // （buildAndFail）。プラグイン側の補足診断は持たず本体診断に委ねる方針のため、その言語エラーが
    // 十分に説明的であること（同一モジュール制約を明示すること）をここで固定する（設計01 §7.2）
    @Test
    fun crossSourceSetInheritorFailsBuild() {
        assertDiagnosticAt(
            failOutput("diag-mpp-cross-source-set", "compileKotlinJvm"),
            "MppCJvm.kt",
            4,
            "e: ",
            DiagFragments.LANG_SEALED_DIFFERENT_MODULE,
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
