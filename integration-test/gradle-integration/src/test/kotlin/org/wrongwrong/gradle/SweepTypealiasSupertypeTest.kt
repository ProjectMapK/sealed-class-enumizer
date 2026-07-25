package org.wrongwrong.gradle

import org.junit.jupiter.api.Disabled
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAnywhere
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent
import kotlin.test.Test

// supertype の頭そのものを typealias で書いた形の掃討（型引数だけを別名にする TC-MAN-069 =
// SweepDiagOkTest との対比）。注入抑止の照合は supertype 生成拡張が受け取る解決済み型で行われるが、
// この段階の型は typealias が未展開であるため、明示形なら注入スキップで受容される 2 形が
// 重複注入によって言語エラー「A supertype appears twice」になる（docs/修正方針案.md #16）。
// 一方、継承者一覧の収集は展開後の supertype を見るため別名でも影響を受けない（TC-MAN-083）。
// 仕様どおりのアサートは @Disabled で保持し、実挙動を固定する回帰ゲートを併置する
// （修正が入ったらゲートが fail して検出できる。その際は @Disabled 側と入れ替える）
class SweepTypealiasSupertypeTest : DiagTestBase() {
    // TC-MAN-081: 基底の手動 Enumized 自体への typealias（展開後は Enumized<SwThSi.Enumish> と厳密一致）
    @Test
    @Disabled("NG#16: 注入抑止の照合が展開前のため重複注入で言語エラーになる — docs/修正方針案.md #16")
    fun typealiasedEnumizedHeadIsAcceptedBySkip() {
        val output = successOutput("sweep-typealias-head", "compileKotlin")
        assertFragmentAbsent(output, DiagFragments.LANG_SUPERTYPE_APPEARS_TWICE)
    }

    // TC-MAN-081 の実挙動固定
    @Test
    fun typealiasedEnumizedHeadCurrentlyDuplicatesSupertype() {
        val output = failOutput("sweep-typealias-head", "compileKotlin")
        assertDiagnosticAnywhere(output, "SwThSi.kt", DiagFragments.LANG_SUPERTYPE_APPEARS_TWICE)
    }

    // TC-MAN-082: 末端 object による生成 Enumish の冗長宣言を typealias で書く形
    //（明示形の TC-DIAG-049 は注入スキップで受容される）
    @Test
    @Disabled("NG#16: 注入抑止の照合が展開前のため重複注入で言語エラーになる — docs/修正方針案.md #16")
    fun typealiasedEnumishHeadIsAcceptedBySkip() {
        val output = successOutput("sweep-typealias-leaf", "compileKotlin")
        assertFragmentAbsent(output, DiagFragments.LANG_SUPERTYPE_APPEARS_TWICE)
    }

    // TC-MAN-082 の実挙動固定
    @Test
    fun typealiasedEnumishHeadCurrentlyDuplicatesSupertype() {
        val output = failOutput("sweep-typealias-leaf", "compileKotlin")
        assertDiagnosticAnywhere(output, "SwTlSi.kt", DiagFragments.LANG_SUPERTYPE_APPEARS_TWICE)
    }

    // TC-MAN-083: 階層内の手動実装（末端 class 自身による生成 Enumish 実装）を typealias で書いても
    // 継承者一覧に載る。手動実装の枝を欠いた kind-when が明示形・別名形とも網羅性エラーになることで
    // 確認する（ビルド失敗が期待される形であり、失敗の内訳が検証の主体）
    @Test
    fun typealiasedManualImplementationIsListedAsInheritor() {
        val output = failOutput("sweep-typealias-impl", "compileKotlin")
        assertDiagnosticAnywhere(output, "UseSwTiAl.kt", DiagFragments.LANG_WHEN_NOT_EXHAUSTIVE, "is SwTiAlLeaf")
        assertDiagnosticAnywhere(output, "UseSwTiEx.kt", DiagFragments.LANG_WHEN_NOT_EXHAUSTIVE, "is SwTiExLeaf")
    }
}
