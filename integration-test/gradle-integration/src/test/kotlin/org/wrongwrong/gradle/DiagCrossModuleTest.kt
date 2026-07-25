package org.wrongwrong.gradle

import kotlin.test.Test
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent

// G 軸: 跨モジュール系（利用側モジュールでの ENUMIZE_AMBIGUOUS_KIND・2 階層への所属・生成 Enumish の
// 別モジュール実装・単一末端サブタイプの吸収・網羅 when の ABI 伝播。docs/テストケース管理.md
// TC-DIAG-021・023・071・078・094・096、docs/概要.md §3・§8、docs/コンパイラプラグイン設計01.md §7.2）
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

    // TC-DIAG-094: 非 sealed の末端 interface 経由で 2 階層に属する型は、Enumized の異なる型引数による
    // 二重継承の言語エラーになる（階層の探索は sealed 連鎖のみを辿るためプラグイン診断は関与しない）
    @Test
    fun crossModuleTwoFamilyImplementationFailsWithLanguageError() {
        assertDiagnosticAt(
            failOutput("diag-cross-families", ":app:compileKotlin"),
            "Cross2.kt",
            7,
            DiagFragments.LANG_INCONSISTENT_TYPE_ARGS,
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
    // プラグイン適用側では ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY も先回り報告される（docs/概要.md §8）
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
