package org.wrongwrong.gradle

import kotlin.test.Test
import org.junit.jupiter.api.Disabled
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAnywhere
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent

// supertype の頭そのものを typealias で書いた形の掃討（型引数だけを別名にする TC-MAN-069 =
// SweepDiagOkTest との対比）。注入抑止の照合は解決済み型を展開してから行い、展開が届かない配置
// （エイリアスが階層より先に解決されない場合）では raw 追跡へ落ちるため、明示形と同じく注入は
// スキップされる。継承者一覧の収集も展開後の supertype を見る（TC-MAN-083）。
// 残る乖離は、その配置で言語側が別名を展開しないまま IR へ進む基底側の 1 形のみ（docs/修正方針案.md #17）
class SweepTypealiasSupertypeTest : DiagTestBase() {
    // TC-MAN-081: 基底の手動 Enumized 自体への typealias（展開後は Enumized<SwThSi.Enumish> と厳密一致）
    @Test
    fun typealiasedEnumizedHeadIsAcceptedBySkip() {
        val output = successOutput("sweep-typealias-head", "compileKotlin")
        assertFragmentAbsent(output, DiagFragments.LANG_SUPERTYPE_APPEARS_TWICE)
        assertFragmentAbsent(output, DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
    }

    // TC-MAN-082: 末端 object による生成 Enumish の冗長宣言を typealias で書く形
    // （明示形の TC-DIAG-049 は注入スキップで受容される）
    @Test
    fun typealiasedEnumishHeadIsAcceptedBySkip() {
        val output = successOutput("sweep-typealias-leaf", "compileKotlin")
        assertFragmentAbsent(output, DiagFragments.LANG_SUPERTYPE_APPEARS_TWICE)
        assertFragmentAbsent(output, DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY)
    }

    // TC-MAN-084: 同一ファイル配置の別名（SwTlSame.kt）。解決済み supertype が展開されない配置でも
    // 末端側の注入はスキップされる（展開前照合だと二重注入で「Inherited platform declarations clash」になる）
    @Test
    fun sameFileTypealiasedEnumishHeadIsAcceptedBySkip() {
        val output = successOutput("sweep-typealias-leaf", "compileKotlin")
        assertFragmentAbsent(output, DiagFragments.LANG_PLATFORM_DECLARATION_CLASH)
    }

    // TC-MAN-085: 頭が Enumized の別名を、エイリアスが階層より先に解決されない配置（同一ファイル）に
    // 置いた形。注入はスキップされるが、言語側が別名を展開しないまま IR へ進むためバックエンド ICE に
    // なる（エイリアスが先に処理される配置 = TC-MAN-081 は成立する）。
    // 仕様どおりのアサートは @Disabled で保持し、実挙動を固定するゲートを併置する
    @Test
    @Disabled("NG#17: 先に解決されない配置の別名を言語側が展開せずバックエンド ICE になる — docs/修正方針案.md #17")
    fun sameFileTypealiasedEnumizedHeadIsAcceptedBySkip() {
        successOutput("sweep-typealias-samefile-head", "compileKotlin")
    }

    // TC-MAN-085 の実挙動固定（docs/修正方針案.md #17 の回帰ゲート）
    @Test
    fun sameFileTypealiasedEnumizedHeadCrashesAsKnownIssue() {
        val output = failOutput("sweep-typealias-samefile-head", "compileKotlin")
        assertDiagnosticAnywhere(output, "Exception during IR fake override builder")
        assertDiagnosticAnywhere(output, "SwSfSi.kt")
    }

    // TC-MAN-083: 階層内の手動実装（末端 class 自身による生成 Enumish 実装）を typealias で書いても
    // 継承者一覧に載る。手動実装の枝を欠いた kind-when が明示形・別名形とも網羅性エラーになることで
    // 確認する（ビルド失敗が期待される形であり、失敗の内訳が検証の主体）
    @Test
    fun typealiasedManualImplementationIsListedAsInheritor() {
        val output = failOutput("sweep-typealias-impl", "compileKotlin")
        assertDiagnosticAnywhere(
            output,
            "UseSwTiAl.kt",
            DiagFragments.LANG_WHEN_NOT_EXHAUSTIVE,
            "is SwTiAlLeaf",
        )
        assertDiagnosticAnywhere(
            output,
            "UseSwTiEx.kt",
            DiagFragments.LANG_WHEN_NOT_EXHAUSTIVE,
            "is SwTiExLeaf",
        )
    }
}
