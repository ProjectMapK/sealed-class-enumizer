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
import org.jetbrains.kotlin.diagnostics.warning0

// 診断カタログ（設計00 §8・設計01 §7.2）。診断 ID は英語の識別子であり、本文の日本語用語とは独立に安定させる。
// ENUMIZE_ENUM_LEAF_UNSUPPORTED は V4 不成立の縮退時のみ有効化するため、定義のみで現状は発火させない。
object EnumizeErrors : KtDiagnosticsContainer() {
    val ENUMIZE_NOT_SEALED: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_ON_EXPECT: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_ON_ACTUAL: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_MULTIPLE_FAMILIES: KtDiagnosticFactory2<String, String> by error2<PsiElement, String, String>()
    val ENUMIZE_NESTED_IN_HIERARCHY: KtDiagnosticFactory1<String> by error1<PsiElement, String>()
    val ENUMIZE_AMBIGUOUS_KIND: KtDiagnosticFactory2<String, String> by error2<PsiElement, String, String>()
    val ENUMIZE_INNER_LEAF: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_KIND_NOT_ACCESSIBLE: KtDiagnosticFactory1<String> by error1<PsiElement, String>()
    val ENUMIZE_KIND_TYPE_NOT_DENOTABLE: KtDiagnosticFactory1<String> by error1<PsiElement, String>()
    val ENUMIZE_COMPANION_LEAF_CONFLICT: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_LABEL_CLASH: KtDiagnosticFactory2<String, String> by error2<PsiElement, String, String>()
    val ENUMIZE_MANUAL_MEMBER_CONFLICT: KtDiagnosticFactory1<String> by error1<PsiElement, String>()
    val ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY: KtDiagnosticFactory1<String> by error1<PsiElement, String>()
    val ENUMIZE_MANUAL_SUPERTYPE_MISMATCH: KtDiagnosticFactory2<String, String> by error2<PsiElement, String, String>()
    val ENUMIZE_RESERVED_NAME_CLASH: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_COMPANION_REQUIRED: KtDiagnosticFactory0 by error0<PsiElement>()
    val ENUMIZE_CROSS_SOURCE_SET: KtDiagnosticFactory1<String> by error1<PsiElement, String>()
    val ENUMIZE_EXTENSION_SHADOWED: KtDiagnosticFactory0 by warning0<PsiElement>()
    val ENUMIZE_ENUM_LEAF_UNSUPPORTED: KtDiagnosticFactory0 by error0<PsiElement>()

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = EnumizeErrorMessages
}
