@file:OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)

package org.wrongwrong.sealedClassEnumizer.compiler.fir.checkers

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirRegularClassChecker
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.getSealedClassInheritors
import org.jetbrains.kotlin.fir.declarations.utils.isActual
import org.jetbrains.kotlin.fir.declarations.utils.isExpect
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinTypeProjection
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.renderReadable
import org.jetbrains.kotlin.name.Name
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames
import org.wrongwrong.sealedClassEnumizer.compiler.fir.EnumizeHierarchyResolver
import org.wrongwrong.sealedClassEnumizer.compiler.fir.EnumizePredicates

// 診断カタログの検査ロジック（設計01 §7.2）。マルチラウンド IC の部分集合ビューで偽陽性を出さないよう、
// すべての検査を「見えている宣言の性質に対する条件検査」として実装する（単調性。設計01 §7.1）
object EnumizeRegularClassChecker : FirRegularClassChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirRegularClass) {
        val symbol = declaration.symbol
        if (symbol.isLocal) return
        val hierarchy = EnumizeHierarchyResolver(context.session)
        val annotated = context.session.predicateBasedProvider.matches(EnumizePredicates.ENUMIZE, declaration)
        val bases = hierarchy.findBases(symbol)
        if (annotated) {
            checkBase(declaration, hierarchy, context, reporter)
            if (bases.isNotEmpty()) {
                reporter.reportOn(
                    declaration.source,
                    EnumizeErrors.ENUMIZE_NESTED_IN_HIERARCHY,
                    bases.first().classId.asFqNameString(),
                    context,
                )
            }
        }
        if (bases.size >= 2) {
            reporter.reportOn(
                declaration.source,
                EnumizeErrors.ENUMIZE_MULTIPLE_FAMILIES,
                bases[0].classId.asFqNameString(),
                bases[1].classId.asFqNameString(),
                context,
            )
        }
        if (annotated || bases.isNotEmpty()) {
            checkLabelShadowing(declaration, hierarchy, context, reporter)
        }
        checkAmbiguousKind(symbol, declaration, hierarchy, context, reporter)
        checkManualEnumishImplementation(declaration, hierarchy, context, reporter)
        val base = bases.singleOrNull()
        if (base != null) {
            checkHierarchyMember(declaration, base, hierarchy, context, reporter)
        }
    }

    // 生成 Enumish の直接実装は階層内（メンバー）と kind に限る。階層外の実装は sealed の
    // 継承者一覧へ反映する経路が無く、JVM では PermittedSubclasses により実行時拒否になるため
    // コンパイル時にエラーとする（V1-(e) の帰結。設計00 §5.2）
    private fun checkManualEnumishImplementation(
        declaration: FirRegularClass,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        for (ref in symbol.resolvedSuperTypeRefs) {
            val superSymbol = hierarchy.tracker.resolveExpandedClassSymbol(ref.coneType) ?: continue
            if (!hierarchy.isOurGenerated(superSymbol)) continue
            if (superSymbol.classId.shortClassName != EnumizeNames.ENUMISH_NAME) continue
            val base = hierarchy.tracker.resolveClassSymbol(superSymbol.classId.outerClassId) ?: continue
            if (isInsideHierarchyOf(symbol, base, hierarchy)) continue
            reporter.reportOn(
                ref.source ?: declaration.source,
                EnumizeErrors.ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY,
                base.classId.asFqNameString(),
                context,
            )
        }
    }

    // 階層のメンバー（末端 class 自身による実装を含む）と、階層の末端の kind を担う companion は
    // 生成 Enumish の正当な実装である
    private fun isInsideHierarchyOf(
        symbol: FirRegularClassSymbol,
        base: FirRegularClassSymbol,
        hierarchy: EnumizeHierarchyResolver,
    ): Boolean {
        if (hierarchy.findBases(symbol).any { it.classId == base.classId }) return true
        if (!symbol.rawStatus.isCompanion) return false
        val outer = hierarchy.tracker.resolveClassSymbol(symbol.classId.outerClassId) ?: return false
        return !hierarchy.isSealed(outer) && hierarchy.findBases(outer).any { it.classId == base.classId }
    }

    // ---- @Enumize 対象（基底）に対する検査 ----

    private fun checkBase(
        declaration: FirRegularClass,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        if (!hierarchy.isSealed(symbol)) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_NOT_SEALED, context)
            return
        }
        if (declaration.isExpect) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_ON_EXPECT, context)
        }
        if (declaration.isActual) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_ON_ACTUAL, context)
        }
        checkReservedNestedName(declaration, hierarchy, context, reporter)
        checkManualEnumizedSupertype(symbol, symbol, symbol, hierarchy, context, reporter)
        // 手動 Enumized<K> の判定は間接継承（interface MyBase : Enumized<K> 経由）も対象とする
        // （docs/エッジケースへの対応方針.md §2）。他の @Enumize 基底は生成された Enumized を持つため除く
        for (superSymbol in hierarchy.supertypeClosure(symbol)) {
            if (hierarchy.isEnumizeBase(superSymbol)) continue
            checkManualEnumizedSupertype(symbol, superSymbol, symbol, hierarchy, context, reporter)
        }
        checkLabelClash(symbol, hierarchy, context, reporter)
        checkCrossSourceSet(declaration, hierarchy, context, reporter)
    }

    private fun checkReservedNestedName(
        declaration: FirRegularClass,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val userEnumish = declaration.declarations.firstOrNull { nested ->
            nested is FirRegularClass &&
                nested.name == EnumizeNames.ENUMISH_NAME &&
                !hierarchy.isOurGenerated(nested.symbol)
        } ?: return
        reporter.reportOn(userEnumish.source, EnumizeErrors.ENUMIZE_RESERVED_NAME_CLASH, context)
    }

    // 型引数の異なる手動継承 `: Enumized<別の型>`。declaring の直接 supertype を検査し、
    // declaring が報告対象自身でない場合（間接継承）は報告対象側の宣言へ位置づける
    private fun checkManualEnumizedSupertype(
        reportTarget: FirRegularClassSymbol,
        declaring: FirRegularClassSymbol,
        base: FirRegularClassSymbol,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val expectedArgument = hierarchy.generatedEnumishClassId(base)
        for (ref in declaring.resolvedSuperTypeRefs) {
            val coneType = ref.coneType as? ConeClassLikeType ?: continue
            if (coneType.classId != EnumizeNames.ENUMIZED_CLASS_ID) continue
            val argument = coneType.typeArguments.firstOrNull() as? ConeKotlinTypeProjection
            val argumentClassId = argument?.type?.classId
            if (argumentClassId != expectedArgument) {
                val source = if (declaring === reportTarget) ref.source ?: reportTarget.source else reportTarget.source
                reporter.reportOn(
                    source,
                    EnumizeErrors.ENUMIZE_MANUAL_SUPERTYPE_MISMATCH,
                    coneType.renderReadable(),
                    "Enumized<${expectedArgument.asFqNameString()}>",
                    context,
                )
            }
        }
    }

    private fun checkLabelClash(
        base: FirRegularClassSymbol,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val groups = hierarchy.leavesOf(base).groupBy(hierarchy::labelOf)
        for ((label, leaves) in groups) {
            if (leaves.size < 2) continue
            for (leaf in leaves) {
                val others = leaves.filter { it !== leaf }
                    .joinToString(separator = ", ") { it.classId.asFqNameString() }
                reporter.reportOn(leaf.source, EnumizeErrors.ENUMIZE_LABEL_CLASH, label, others, context)
            }
        }
    }

    // コンパイラ本体の診断（継承者の別ソースセット逸脱）への補足説明
    private fun checkCrossSourceSet(
        declaration: FirRegularClass,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        for (inheritorId in declaration.getSealedClassInheritors(context.session)) {
            val inheritor = hierarchy.tracker.resolveClassSymbol(inheritorId) ?: continue
            if (inheritor.moduleData != declaration.moduleData) {
                reporter.reportOn(
                    declaration.source,
                    EnumizeErrors.ENUMIZE_CROSS_SOURCE_SET,
                    inheritorId.asFqNameString(),
                    context,
                )
            }
        }
    }

    // ---- 階層メンバー（中間 sealed・末端）に対する検査 ----

    private fun checkHierarchyMember(
        declaration: FirRegularClass,
        base: FirRegularClassSymbol,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        checkManualEnumizedSupertype(symbol, symbol, base, hierarchy, context, reporter)
        if (hierarchy.isSealed(symbol)) return
        if (declaration.status.isInner) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_INNER_LEAF, context)
            return
        }
        checkKindAccessibility(declaration, base, hierarchy, context, reporter)
        checkManualMemberConflicts(declaration, base, hierarchy, context, reporter)
        if (symbol.classKind == ClassKind.OBJECT) return
        checkCompanionOfLeafClass(declaration, base, hierarchy, context, reporter)
    }

    private fun checkKindAccessibility(
        declaration: FirRegularClass,
        base: FirRegularClassSymbol,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        val isPrivateTopLevel =
            symbol.classId.outerClassId == null && declaration.status.visibility == Visibilities.Private
        if (isPrivateTopLevel && !inSameFile(symbol, base, context)) {
            reporter.reportOn(
                declaration.source,
                EnumizeErrors.ENUMIZE_KIND_NOT_ACCESSIBLE,
                "private top-level leaf '${symbol.classId.asFqNameString()}' declared in another file",
                context,
            )
        }
    }

    private fun checkCompanionOfLeafClass(
        declaration: FirRegularClass,
        base: FirRegularClassSymbol,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        val companion = symbol.companionObjectSymbol
        if (companion == null) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_COMPANION_REQUIRED, context)
            return
        }
        if (hierarchy.isOurGenerated(companion)) return
        val companionVisibility = companion.rawStatus.visibility
        if (companionVisibility == Visibilities.Private || companionVisibility == Visibilities.Protected) {
            reporter.reportOn(
                companion.source,
                EnumizeErrors.ENUMIZE_KIND_NOT_ACCESSIBLE,
                "companion object of '${symbol.classId.asFqNameString()}' is ${companionVisibility.name}",
                context,
            )
        }
        if (hierarchy.isLeaf(companion)) {
            reporter.reportOn(companion.source, EnumizeErrors.ENUMIZE_COMPANION_LEAF_CONFLICT, context)
        }
        val denotable = hierarchy.effectiveVisibilityAtLeast(companion, symbol) ||
            hierarchy.effectiveVisibilityAtLeast(base, symbol)
        if (!denotable) {
            reporter.reportOn(
                declaration.source,
                EnumizeErrors.ENUMIZE_KIND_TYPE_NOT_DENOTABLE,
                symbol.classId.asFqNameString(),
                context,
            )
        }
    }

    // 生成対象メンバーの手動宣言・階層外 interface からの具象実装の継承（toString は対象外 = 設計01 §7.2）
    private fun checkManualMemberConflicts(
        declaration: FirRegularClass,
        base: FirRegularClassSymbol,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        val isObjectLeaf = symbol.classKind == ClassKind.OBJECT
        val leafNames = if (isObjectLeaf) {
            setOf(EnumizeNames.LABEL, EnumizeNames.ENUMIZED_CLASS_PROPERTY, EnumizeNames.AS_ENUMISH)
        } else {
            setOf(EnumizeNames.AS_ENUMISH)
        }
        reportConflicts(declaration, symbol, leafNames, base, hierarchy, context, reporter)
        if (isObjectLeaf) return
        val companion = symbol.companionObjectSymbol ?: return
        if (hierarchy.isOurGenerated(companion)) return
        val kindNames = setOf(EnumizeNames.LABEL, EnumizeNames.ENUMIZED_CLASS_PROPERTY)
        reportConflicts(companion.fir, companion, kindNames, base, hierarchy, context, reporter)
    }

    private fun reportConflicts(
        declaration: FirRegularClass,
        symbol: FirRegularClassSymbol,
        names: Set<Name>,
        base: FirRegularClassSymbol,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        for (member in manualMembersNamed(declaration, names, hierarchy)) {
            reporter.reportOn(
                member.source,
                EnumizeErrors.ENUMIZE_MANUAL_MEMBER_CONFLICT,
                memberNameOf(member)?.asString().orEmpty(),
                context,
            )
        }
        for (name in inheritedConcreteConflicts(symbol, names, base, hierarchy)) {
            reporter.reportOn(
                declaration.source,
                EnumizeErrors.ENUMIZE_MANUAL_MEMBER_CONFLICT,
                name.asString(),
                context,
            )
        }
    }

    private fun manualMembersNamed(
        declaration: FirRegularClass,
        names: Set<Name>,
        hierarchy: EnumizeHierarchyResolver,
    ): List<FirDeclaration> =
        declaration.declarations.filter { member ->
            val name = memberNameOf(member)
            name != null && name in names && !hierarchy.isOurGeneratedDeclaration(member)
        }

    private fun memberNameOf(declaration: FirDeclaration): Name? = when (declaration) {
        is FirNamedFunction -> declaration.name
        is FirProperty -> declaration.name
        else -> null
    }

    // 階層外のユーザー interface から同名メンバーの default 実装（具象）を継承している構成の検出
    private fun inheritedConcreteConflicts(
        symbol: FirRegularClassSymbol,
        names: Set<Name>,
        base: FirRegularClassSymbol,
        hierarchy: EnumizeHierarchyResolver,
    ): List<Name> {
        val excludedClassIds = setOf(
            EnumizeNames.ENUMISH_CLASS_ID,
            EnumizeNames.ENUMISH_COMPANION_CLASS_ID,
            EnumizeNames.ENUMIZED_CLASS_ID,
            hierarchy.generatedEnumishClassId(base),
            hierarchy.generatedEnumishCompanionClassId(base),
        )
        val foreignInterfaces = hierarchy.supertypeClosure(symbol).filter { superSymbol ->
            superSymbol.classKind == ClassKind.INTERFACE &&
                superSymbol.classId !in excludedClassIds &&
                hierarchy.findBases(superSymbol).isEmpty() &&
                !hierarchy.isEnumizeBase(superSymbol)
        }
        return names.filter { name ->
            foreignInterfaces.any { iface -> declaresConcreteMember(iface, name) }
        }
    }

    private fun declaresConcreteMember(symbol: FirRegularClassSymbol, name: Name): Boolean =
        symbol.fir.declarations.any { member ->
            when (member) {
                is FirNamedFunction -> member.name == name && member.body != null
                is FirProperty ->
                    member.name == name && (member.getter?.body != null || member.initializer != null)
                else -> false
            }
        }

    // ---- kind の一意対応（AMBIGUOUS_KIND）: 階層内・利用側（プラグイン適用モジュール）の双方 ----

    private fun checkAmbiguousKind(
        symbol: FirRegularClassSymbol,
        declaration: FirRegularClass,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val leafAncestors = hierarchy.supertypeClosure(symbol).filter(hierarchy::isLeaf)
        val kindCandidates = buildList {
            if (hierarchy.isLeaf(symbol)) add(symbol)
            addAll(leafAncestors)
        }
        if (kindCandidates.size < 2) return
        val sameBaseGroup = kindCandidates
            .groupBy { hierarchy.findSingleBase(it)?.classId }
            .entries.firstOrNull { (baseId, group) -> baseId != null && group.size >= 2 }
            ?: return
        reporter.reportOn(
            declaration.source,
            EnumizeErrors.ENUMIZE_AMBIGUOUS_KIND,
            sameBaseGroup.value[0].classId.asFqNameString(),
            sameBaseGroup.value[1].classId.asFqNameString(),
            context,
        )
    }

    // ---- 拡張シャドーイング警告 ----

    private fun checkLabelShadowing(
        declaration: FirRegularClass,
        hierarchy: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        for (member in declaration.declarations) {
            val name = memberNameOf(member) ?: continue
            if (name != EnumizeNames.LABEL) continue
            if (hierarchy.isOurGeneratedDeclaration(member)) continue
            val visibility = when (member) {
                is FirNamedFunction -> member.status.visibility
                is FirProperty -> member.status.visibility
                else -> continue
            }
            if (visibility == Visibilities.Private) continue
            reporter.reportOn(member.source, EnumizeErrors.ENUMIZE_EXTENSION_SHADOWED, context)
        }
    }

    private fun inSameFile(
        first: FirRegularClassSymbol,
        second: FirRegularClassSymbol,
        context: CheckerContext,
    ): Boolean {
        val provider = context.session.firProvider
        val firstFile = provider.getFirClassifierContainerFileIfAny(first)
        val secondFile = provider.getFirClassifierContainerFileIfAny(second)
        return firstFile != null && firstFile === secondFile
    }
}
