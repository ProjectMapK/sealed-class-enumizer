package org.wrongwrong.gradle

import org.junit.jupiter.api.Disabled
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt
import kotlin.test.Test

// G 軸: companion 系診断（ENUMIZE_COMPANION_LEAF_CONFLICT / ENUMIZE_COMPANION_REQUIRED。
// docs/テストケース管理.md TC-DIAG-036〜038・057〜060・101〜102・108、設計01 §6・§7.2）
class DiagCompanionTest : DiagTestBase() {
    // TC-DIAG-036: 末端 class の companion が同一階層の末端（報告位置 = companion）
    @Test
    fun companionAsLeafOfSameHierarchyConflicts() {
        assertDiagnosticAt(
            failOutput("diag-companion-conflict", "compileKotlin"),
            "ClcHost.kt",
            5,
            DiagFragments.COMPANION_LEAF_CONFLICT,
        )
    }

    // TC-DIAG-108: named companion（既定名以外）でも同様に発火する
    @Test
    fun namedCompanionAsLeafConflicts() {
        assertDiagnosticAt(
            failOutput("diag-companion-conflict", "compileKotlin"),
            "Clc2Host.kt",
            5,
            DiagFragments.COMPANION_LEAF_CONFLICT,
        )
    }

    // TC-DIAG-057: typealias 経由 supertype の末端 class に companion 無し（raw-ref 追跡は typealias を
    // 意図的に辿らず、明示 companion を促す契約）
    @Test
    fun typealiasLeafWithoutCompanionRequiresCompanion() {
        assertDiagnosticAt(
            failOutput("diag-companion-required", "compileKotlin"),
            "CrFoo.kt",
            4,
            DiagFragments.COMPANION_REQUIRED,
        )
    }

    // TC-DIAG-058: doc は import エイリアス経由でも ENUMIZE_COMPANION_REQUIRED を要求するが、実測は
    // プラグイン診断ゼロで asEnumish 等の生成も欠落し、言語の「does not implement abstract member」に合流する
    @Test
    @Disabled("NG: import エイリアス経由の末端が階層メンバーとして扱われず COMPANION_REQUIRED 不発火 — docs/修正方針案.md 反映待ち")
    fun importAliasLeafWithoutCompanionRequiresCompanion() {
        assertDiagnosticAt(
            failOutput("diag-companion-required", "compileKotlin"),
            "CrBar.kt",
            6,
            DiagFragments.COMPANION_REQUIRED,
        )
    }

    // TC-DIAG-037/041/060/102 の near-miss は DiagNearMissTest、TC-DIAG-038/059 は producer-jvm が実証済み
    // （Labeled.Styled = SI 非実装の通常 companion・SI.Foo = 直接名参照での companion 自動生成）。
    // TC-DIAG-101（V3 全滅縮退時の常時要求）は縮退モードを駆動する機構がプラグインに無いため保留
}
