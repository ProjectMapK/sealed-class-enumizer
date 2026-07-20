package org.wrongwrong.gradle

import org.junit.jupiter.api.Disabled
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent
import kotlin.test.Test

// G 軸: 跨モジュール系（利用側モジュールでの ENUMIZE_AMBIGUOUS_KIND・2 家族実装・生成 Enumish の
// 別モジュール実装・単一末端サブタイプの吸収・網羅 when の ABI 伝播。docs/テストケース管理.md
// TC-DIAG-021・023・071・078・094・096、docs/概要.md §3・§8、設計01 §7.2）
class DiagCrossModuleTest : DiagTestBase() {
    // TC-DIAG-021: 別モジュール（プラグイン適用）での複数末端 interface 実装 → 利用側で AMBIGUOUS_KIND
    @Test
    fun crossModuleMultiLeafImplementationIsAmbiguous() {
        assertDiagnosticAt(
            failOutput("diag-cross-ambiguous", ":app:compileKotlin"),
            "Cross.kt",
            6,
            DiagFragments.AMBIGUOUS_KIND,
            "org.wrongwrong.diag.xamb.XambSi.LeafA",
            "org.wrongwrong.diag.xamb.XambSi.LeafB",
        )
    }

    // TC-DIAG-094: 別モジュールで 2 家族の末端 interface を実装 → 異なる型引数の二重継承の言語エラー
    @Test
    fun crossModuleTwoFamilyImplementationFailsWithLanguageError() {
        assertDiagnosticAt(
            failOutput("diag-cross-families", ":app:compileKotlin"),
            "Cross2.kt",
            7,
            DiagFragments.LANG_INCONSISTENT_TYPE_ARGS,
        )
    }

    // TC-DIAG-094: doc は「プラグイン適用時は ENUMIZE_MULTIPLE_FAMILIES でも先回り報告」とするが、
    // 実測は言語エラーのみで不発火（家族探索が sealed 連鎖のみを上向きに辿るため、非 sealed の
    // 末端 interface を経由する 2 家族実装では基底に到達しない）。報告 ID の選択は doc 側でも
    // プロトタイプ確定待ちの残ギャップとされている
    @Test
    @Disabled("NG: 跨モジュール 2 家族実装で ENUMIZE_MULTIPLE_FAMILIES が不発火（言語エラーのみ） — docs/修正方針案.md 反映待ち")
    fun crossModuleTwoFamilyImplementationReportsMultipleFamilies() {
        assertDiagnosticAt(
            failOutput("diag-cross-families", ":app:compileKotlin"),
            "Cross2.kt",
            7,
            DiagFragments.MULTIPLE_FAMILIES,
        )
    }

    // TC-DIAG-096: プラグイン未適用の利用側では言語エラーのみ（ENUMIZE_AMBIGUOUS_KIND は非発火）
    @Test
    fun withoutPluginOnlyLanguageErrorIsReported() {
        val output = failOutput("diag-cross-ambiguous-noplugin", ":app:compileKotlin")
        assertDiagnosticAt(output, "Cross3.kt", 6, DiagFragments.LANG_MANY_IMPL_MEMBER)
        assertFragmentAbsent(output, DiagFragments.AMBIGUOUS_KIND)
    }

    // TC-DIAG-071: 生成 Enumish は sealed（V1）のため別モジュールからの手動実装は言語側で不可。
    // プラグイン適用側では ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY も先回り報告される（現行仕様 = 概要 §8。
    // テストケース管理.md MI-04 行の「非発火」は V1-(e) 反映前の記述）
    @Test
    fun crossModuleRogueImplementationFailsBySealedRule() {
        val output = failOutput("diag-cross-rogue", ":app:compileKotlin")
        assertDiagnosticAt(output, "Rogue.kt", 7, DiagFragments.LANG_SEALED_DIFFERENT_MODULE)
        assertDiagnosticAt(output, "Rogue.kt", 7, DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY)
    }

    // TC-DIAG-023: 別モジュールでの単一末端サブタイプは kind 吸収で非発火（ビルド成功）
    @Test
    fun crossModuleSingleLeafSubtypeIsAbsorbed() {
        val output = successOutput("diag-cross-absorb", ":app:compileKotlin")
        assertFragmentAbsent(output, DiagFragments.AMBIGUOUS_KIND)
        assertFragmentAbsent(output, DiagFragments.INNER_LEAF)
    }

    // TC-DIAG-078: 生成 Enumish 上の網羅 when（else なし・V1-a）が成立し、ライブラリ側の末端追加で
    // 利用側の再コンパイルが非網羅エラーになる（V1-b の観測点。診断は言語側の網羅性検査）
    @Test
    fun addingLeafBreaksExhaustiveKindWhenInConsumer() {
        val dir = prepare("diag-cross-when-exhaustive")
        TestKitHarness.build(dir, ":app:compileKotlin")
        TestKitHarness.replaceInFile(
            dir,
            "lib/src/main/kotlin/org/wrongwrong/diag/xwe/WeSi.kt",
            "    data object B : WeSi",
            "    data object B : WeSi\n\n    data object C : WeSi",
        )
        val failed = TestKitHarness.buildAndFail(dir, ":app:compileKotlin")
        assertDiagnosticAt(failed.output, "UseWhen.kt", 6, DiagFragments.LANG_WHEN_NOT_EXHAUSTIVE)
    }
}
