package io.github.projectmapk.gradle

import io.github.projectmapk.gradle.DiagAsserts.assertDiagnosticAt
import kotlin.test.Test
import kotlin.test.assertEquals

// 実挙動固定 4 態様（docs/test/ケース04-診断.md DIA-66〜69）。
// 各メソッドは実測で確定した挙動を回帰ゲートとして固定する
// （probe-* フィクスチャは 1 態様 1 フィクスチャ。ICE 等の既知の制限は docs/test/保留.md §2）
class ProbeGateTest : DiagTestBase() {
    // docs/test/ケース04-診断.md DIA-66: companion 無し末端 class の非 companion ネスト宣言
    // object Companion は自動生成 companion と言語の再宣言衝突（Conflicting declarations）になり
    // ビルド失敗する（class 宣言と object Companion 宣言の双方へ報告）
    @Test
    fun companionNameClashBehaviorIsPinned() {
        val output = failOutput("probe-companion-clash", "compileKotlin")
        assertDiagnosticAt(output, "CcLeaf.kt", 6, DiagFragments.LANG_CONFLICTING_DECLARATIONS)
        assertDiagnosticAt(output, "CcLeaf.kt", 7, DiagFragments.LANG_CONFLICTING_DECLARATIONS)
    }

    // docs/test/ケース04-診断.md DIA-67: @Enumize の import 別名・typealias 表記は述語
    // （エイリアス展開前に確定）に載らず生成が走らないため ENUMIZE_ALIASED_ANNOTATION。
    // FQN 表記のみ生成へ到達する
    @Test
    fun annotationNotationBehaviorIsPinned() {
        val dir = prepare("probe-annotation-alias")
        val aliased = TestKitHarness.buildAndFail(dir, "compileKotlin")
        assertDiagnosticAt(aliased.output, "AaImport.kt", 7, DiagFragments.ALIASED_ANNOTATION)
        assertDiagnosticAt(aliased.output, "AaTypealias.kt", 5, DiagFragments.ALIASED_ANNOTATION)
        TestKitHarness.deleteFile(
            dir,
            "src/main/kotlin/io/github/projectmapk/probe/alias/AaImport.kt",
        )
        TestKitHarness.deleteFile(
            dir,
            "src/main/kotlin/io/github/projectmapk/probe/alias/AaTypealias.kt",
        )
        val out = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(listOf("FQ=F1"), out)
    }

    // docs/test/ケース04-診断.md DIA-68: 多段 private 壁（private トップレベルクラス内のネスト kind）
    // でもファイル単位のトップレベルアクセサが両壁を跨ぎ、entries が全末端へ到達する
    @Test
    fun deepPrivateWallBehaviorIsPinned() {
        val dir = prepare("probe-deep-private")
        val out = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(listOf("ENTRIES=Leaf,Open"), out)
    }

    // docs/test/ケース04-診断.md DIA-69: 基底へのクラス委譲末端は宣言衝突なくコンパイル成立し、
    // 生成 override が委譲実装に勝つ（asEnumish は自身の kind を返し kind 契約は維持・entries にも載る）
    @Test
    fun delegationLeafBehaviorIsPinned() {
        val dir = prepare("probe-delegation")
        val out = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(listOf("DEL=Del", "ENTRIES=Del,Ok"), out)
    }
}
