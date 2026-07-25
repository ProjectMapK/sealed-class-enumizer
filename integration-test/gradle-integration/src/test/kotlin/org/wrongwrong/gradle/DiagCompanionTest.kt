package org.wrongwrong.gradle

import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt
import kotlin.test.Test

// G 軸: companion 系診断（ENUMIZE_COMPANION_LEAF_CONFLICT。
// docs/テストケース管理.md TC-DIAG-036・108、設計01 §6・§7.2）
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

    // TC-DIAG-037/041/060/102 の near-miss は DiagNearMissTest、TC-DIAG-038/059 は producer-jvm が実証済み
    // （Labeled.Styled = SI 非実装の通常 companion・SI.Foo = 直接名参照での companion 自動生成）。
    // ENUMIZE_COMPANION_REQUIRED は候補判定が supertype の全表記を扱うようになり削除済み（設計01 §6.2）。
    // 旧発火ケース TC-DIAG-057/058 は「表記に依らず kind が揃う」検証として DiagNearMissTest が持つ。
    // TC-DIAG-101（V3 全滅縮退時の常時要求）は縮退モードを駆動する機構がプラグインに無いため保留
}
