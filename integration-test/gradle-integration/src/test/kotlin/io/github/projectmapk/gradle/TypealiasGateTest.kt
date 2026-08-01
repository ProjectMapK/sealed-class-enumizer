package io.github.projectmapk.gradle

import io.github.projectmapk.gradle.DiagAsserts.assertDiagnosticAnywhere
import kotlin.test.Test
import org.junit.jupiter.api.Disabled

// typealias 表記の境界（docs/test/ケース04-診断.md DIA-59・DIA-52 の既知の制限ゲート）。
// 手動実装の別名表記の inheritors 登載と、同一ファイル頭別名の言語 ICE（既知の制限）を固定する
class TypealiasGateTest : DiagTestBase() {
    // docs/test/ケース04-診断.md DIA-59: typealias 経由の階層内手動実装も inheritors に登載される
    // （手動実装枝を欠く kind-when が明示形・別名形とも非網羅エラーになることで観測する）
    @Test
    fun typealiasedManualImplementationIsListedAsInheritor() {
        val output = failOutput("sweep-typealias-impl", "compileKotlin")
        assertDiagnosticAnywhere(
            output,
            "SwTiAl.kt",
            DiagFragments.LANG_WHEN_NOT_EXHAUSTIVE,
            "is SwTiAlLeaf",
        )
        assertDiagnosticAnywhere(
            output,
            "SwTiEx.kt",
            DiagFragments.LANG_WHEN_NOT_EXHAUSTIVE,
            "is SwTiExLeaf",
        )
    }

    // docs/test/ケース04-診断.md DIA-52 の既知の制限: 同一ファイル頭別名（Enumized への typealias が
    // 階層より先に解決されない配置）の仕様どおりの成功アサートを @Disabled で保持する
    @Test
    @Disabled("先に解決されない配置の別名を言語側が展開せずバックエンド ICE になる — docs/概要.md §7 の既知の制限")
    fun sameFileTypealiasedEnumizedHeadIsAcceptedBySkip() {
        successOutput("sweep-typealias-samefile-head", "compileKotlin")
    }

    // 既知の制限の実挙動固定（言語側が解消したら fail して検出できる回帰ゲート）
    @Test
    fun sameFileTypealiasedEnumizedHeadCrashesAsKnownIssue() {
        val output = failOutput("sweep-typealias-samefile-head", "compileKotlin")
        assertDiagnosticAnywhere(output, "Exception during IR fake override builder")
        assertDiagnosticAnywhere(output, "SwSfSi.kt")
    }
}
