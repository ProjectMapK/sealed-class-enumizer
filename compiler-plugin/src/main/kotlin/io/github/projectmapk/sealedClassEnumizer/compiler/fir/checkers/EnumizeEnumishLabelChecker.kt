package io.github.projectmapk.sealedClassEnumizer.compiler.fir.checkers

import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeNames
import io.github.projectmapk.sealedClassEnumizer.compiler.fir.enumizeHierarchyResolver
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirRegularClassChecker
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getStringArgument
import org.jetbrains.kotlin.fir.declarations.utils.isLocal

// @EnumishLabel の妥当性検査（docs/エッジケースへの対応方針.md §3）: 付与先が label の由来となる
// 末端宣言であること・値が空白のみでないこと。基底・中間 sealed・階層外（kind の companion を含む）への
// 付与は前者のエラーになる。複数階層への所属（MULTIPLE_HIERARCHIES のエラー構成）は付与先としては
// 末端であり、所属側の診断に委ねて重ねては報告しない
object EnumizeEnumishLabelChecker : FirRegularClassChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirRegularClass) {
        val symbol = declaration.symbol
        if (symbol.isLocal) return
        val annotation =
            symbol.getAnnotationByClassId(
                EnumizeNames.ENUMISH_LABEL_ANNOTATION_CLASS_ID,
                context.session,
            ) ?: return
        val resolver = context.session.enumizeHierarchyResolver
        val source = annotation.source ?: declaration.source
        if (resolver.basesOf(symbol).isEmpty() || resolver.isSealed(symbol)) {
            reporter.reportOn(
                source,
                EnumizeErrors.ENUMIZE_INVALID_LABEL,
                "it is applicable only to a leaf declaration of an '@Enumize' hierarchy. " +
                    "Remove the annotation or move it to the leaf declaration.",
                context,
            )
            return
        }
        val value = annotation.getStringArgument(EnumizeNames.VALUE_PARAMETER)
        if (value != null && value.isBlank()) {
            reporter.reportOn(
                source,
                EnumizeErrors.ENUMIZE_INVALID_LABEL,
                "the label must not be blank. Specify a non-blank label.",
                context,
            )
        }
    }
}
