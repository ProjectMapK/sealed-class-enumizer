package org.wrongwrong.sealedClassEnumizer.compiler.fir.checkers

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory2
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.error2
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.warning1

// 診断カタログ（docs/コンパイラプラグイン設計00.md §8・docs/コンパイラプラグイン設計01.md §7.2）。
// 診断 ID は英語の識別子であり、本文の日本語用語とは独立に安定させる
object EnumizeErrors : KtDiagnosticsContainer() {
    val ENUMIZE_NOT_SEALED: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_ON_EXPECT: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_ON_ACTUAL: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_MULTIPLE_HIERARCHIES: KtDiagnosticFactory2<String, String> by
        error2<PsiElement, String, String>()
    val ENUMIZE_NESTED_IN_HIERARCHY: KtDiagnosticFactory1<String> by error1<PsiElement, String>()
    val ENUMIZE_AMBIGUOUS_KIND: KtDiagnosticFactory2<String, String> by
        error2<PsiElement, String, String>()
    val ENUMIZE_INNER_LEAF: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_KIND_TYPE_NOT_DENOTABLE: KtDiagnosticFactory1<String> by
        error1<PsiElement, String>()
    val ENUMIZE_COMPANION_LEAF_CONFLICT: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_LABEL_CLASH: KtDiagnosticFactory2<String, String> by
        error2<PsiElement, String, String>()
    val ENUMIZE_MANUAL_MEMBER_CONFLICT: KtDiagnosticFactory1<String> by error1<PsiElement, String>()
    val ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY: KtDiagnosticFactory1<String> by
        error1<PsiElement, String>()
    val ENUMIZE_MANUAL_SUPERTYPE_MISMATCH: KtDiagnosticFactory2<String, String> by
        error2<PsiElement, String, String>()
    val ENUMIZE_RESERVED_NAME_CLASH: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_EXTENSION_SHADOWED: KtDiagnosticFactory1<String> by warning1<PsiElement, String>()

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = EnumizeErrorMessages
}
