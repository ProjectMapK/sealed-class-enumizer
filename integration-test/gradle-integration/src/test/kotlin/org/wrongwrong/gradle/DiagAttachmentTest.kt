package org.wrongwrong.gradle

import kotlin.test.Test
import kotlin.test.assertTrue
import org.wrongwrong.gradle.DiagAsserts.assertDiagnosticAt
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsentAt

// G 軸: @Enumize の付与対象系診断（ENUMIZE_NOT_SEALED と付与先の境界。docs/テストケース管理.md
// TC-DIAG-001〜008・085〜087、docs/コンパイラプラグイン設計01.md §7.2、docs/概要.md §8）
class DiagAttachmentTest : DiagTestBase() {
    private fun notSealed(): String = failOutput("diag-not-sealed", "compileKotlin")

    // TC-DIAG-001: enum class への付与
    @Test
    fun enumClassIsNotSealed() {
        assertDiagnosticAt(notSealed(), "NsEnum.kt", 6, DiagFragments.NOT_SEALED)
    }

    // TC-DIAG-002: annotation class への付与
    @Test
    fun annotationClassIsNotSealed() {
        assertDiagnosticAt(notSealed(), "NsAnnotation.kt", 6, DiagFragments.NOT_SEALED)
    }

    // TC-DIAG-003: object / data object への付与（両亜種）。object 宣言の報告位置は class 系と異なり
    // アノテーション行でなく object キーワード行になる（実測）
    @Test
    fun objectVariantsAreNotSealed() {
        assertDiagnosticAt(notSealed(), "NsObject.kt", 7, DiagFragments.NOT_SEALED)
        assertDiagnosticAt(notSealed(), "NsDataObject.kt", 7, DiagFragments.NOT_SEALED)
    }

    // TC-DIAG-004: open / abstract / final class への付与（3 亜種）
    @Test
    fun nonSealedClassVariantsAreNotSealed() {
        assertDiagnosticAt(notSealed(), "NsOpenClass.kt", 6, DiagFragments.NOT_SEALED)
        assertDiagnosticAt(notSealed(), "NsAbstractClass.kt", 6, DiagFragments.NOT_SEALED)
        assertDiagnosticAt(notSealed(), "NsFinalClass.kt", 6, DiagFragments.NOT_SEALED)
    }

    // TC-DIAG-005: 非 sealed interface への付与
    @Test
    fun plainInterfaceIsNotSealed() {
        assertDiagnosticAt(notSealed(), "NsInterface.kt", 6, DiagFragments.NOT_SEALED)
    }

    // TC-DIAG-006: fun interface（非 sealed）への付与
    @Test
    fun funInterfaceIsNotSealed() {
        assertDiagnosticAt(notSealed(), "NsFunInterface.kt", 6, DiagFragments.NOT_SEALED)
    }

    // TC-DIAG-087: value class / data class への付与（final class 亜種は TC-DIAG-004 の NsFinalClass）
    @Test
    fun valueAndDataClassAreNotSealed() {
        assertDiagnosticAt(notSealed(), "NsValueClass.kt", 6, DiagFragments.NOT_SEALED)
        assertDiagnosticAt(notSealed(), "NsDataClass.kt", 6, DiagFragments.NOT_SEALED)
    }

    // TC-DIAG-085: 非 sealed 末端への @Enumize は NOT_SEALED / NESTED_IN_HIERARCHY の双方の条件を満たす。
    // doc は「少なくとも 1 つ以上の発火」を要求（実測は両方発火）
    @Test
    fun annotatedNonSealedLeafReportsAtLeastOneError() {
        val output = notSealed()
        val positioned = output.lineSequence().filter { it.contains("NsNested.kt:8:") }.toList()
        assertTrue(
            positioned.any {
                it.contains(DiagFragments.NOT_SEALED) ||
                    it.contains(DiagFragments.NESTED_IN_HIERARCHY)
            },
            "NsNested.kt:8 に NOT_SEALED / NESTED_IN_HIERARCHY のいずれも無い: $positioned",
        )
    }

    // TC-DIAG-086: CLASS 以外のターゲット（typealias / fun / val）へは言語の WRONG_ANNOTATION_TARGET が出て
    // プラグイン診断には到達しない
    @Test
    fun wrongTargetsFailWithLanguageErrorOnly() {
        val output = notSealed()
        assertDiagnosticAt(output, "NsTargets.kt", 6, DiagFragments.LANG_WRONG_ANNOTATION_TARGET)
        assertDiagnosticAt(output, "NsTargets.kt", 9, DiagFragments.LANG_WRONG_ANNOTATION_TARGET)
        assertDiagnosticAt(output, "NsTargets.kt", 12, DiagFragments.LANG_WRONG_ANNOTATION_TARGET)
        assertFragmentAbsentAt(output, "NsTargets.kt", DiagFragments.NOT_SEALED)
    }

    // TC-DIAG-007(V9): sealed fun interface は言語側で不許容（'sealed fun interface' is unsupported.）。
    // 言語エラーへ合流し、プラグインの NOT_SEALED には到達しない分岐を確認する
    @Test
    fun sealedFunInterfaceMergesIntoLanguageError() {
        val output = failOutput("diag-sealed-fun-interface", "compileKotlin")
        assertDiagnosticAt(output, "SealedFun.kt", 7, DiagFragments.LANG_SEALED_FUN_INTERFACE)
        assertFragmentAbsent(output, DiagFragments.NOT_SEALED)
    }

    // TC-DIAG-008 は producer-jvm が実証済み（sealed interface = SI.kt・sealed class = Generic.kt が
    // @Enumize 付きでビルド成功 = NOT_SEALED 非発火）のため本クラスでは重複実装しない（実装済(既存)）
}
