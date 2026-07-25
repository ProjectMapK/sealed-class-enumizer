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
import org.jetbrains.kotlin.fir.declarations.utils.isActual
import org.jetbrains.kotlin.fir.declarations.utils.isExpect
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
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
import org.wrongwrong.sealedClassEnumizer.compiler.fir.EnumizeMembership
import org.wrongwrong.sealedClassEnumizer.compiler.fir.EnumizePredicates
import org.wrongwrong.sealedClassEnumizer.compiler.fir.enumizeHierarchyResolver

// 診断カタログの検査ロジック（docs/コンパイラプラグイン設計01.md §7.2）。マルチラウンド IC の部分集合ビューで偽陽性を出さないよう、
// すべての検査を「見えている宣言の性質に対する条件検査」として実装する（単調性。docs/コンパイラプラグイン設計01.md §7.1）。
// 検査対象クラスの所属（EnumizeMembership）は入口で一度だけ resolver から読み、各検査へ引数で受け渡す
object EnumizeRegularClassChecker : FirRegularClassChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirRegularClass) {
        val symbol = declaration.symbol
        if (symbol.isLocal) return
        val resolver = context.session.enumizeHierarchyResolver
        val annotated =
            context.session.predicateBasedProvider.matches(EnumizePredicates.ENUMIZE, declaration)
        // 階層の一意性を見る診断だけは異常状態（非所属・複数所属）の内訳が要るため一覧を生読みする。
        // 以降の検査へ取り回すのは正常な所属（membership。異常時は null）のみ
        val belongingBases = resolver.basesOf(symbol)
        val membership = resolver.membershipOf(symbol)
        if (annotated) {
            checkBase(declaration, resolver, context, reporter)
            if (belongingBases.isNotEmpty()) {
                reporter.reportOn(
                    declaration.source,
                    EnumizeErrors.ENUMIZE_NESTED_IN_HIERARCHY,
                    belongingBases.first().classId.asFqNameString(),
                    context,
                )
            }
        }
        if (belongingBases.size >= 2) {
            reporter.reportOn(
                declaration.source,
                EnumizeErrors.ENUMIZE_MULTIPLE_FAMILIES,
                belongingBases[0].classId.asFqNameString(),
                belongingBases[1].classId.asFqNameString(),
                context,
            )
        }
        if (annotated || belongingBases.isNotEmpty()) {
            checkLabelShadowing(declaration, membership, resolver, context, reporter)
        }
        checkAmbiguousKind(declaration, membership, resolver, context, reporter)
        checkManualEnumishImplementation(declaration, membership, resolver, context, reporter)
        if (membership != null) {
            checkHierarchyMember(declaration, membership, resolver, context, reporter)
        }
    }

    // ---- @Enumize 対象（基底）に対する検査 ----

    private fun checkBase(
        declaration: FirRegularClass,
        resolver: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        if (!resolver.isSealed(symbol)) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_NOT_SEALED, context)
            return
        }
        if (declaration.isExpect) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_ON_EXPECT, context)
        }
        if (declaration.isActual) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_ON_ACTUAL, context)
        }
        checkReservedNestedName(declaration, resolver, context, reporter)
        checkManualEnumizedSupertype(symbol, symbol, symbol, resolver, context, reporter)
        // 手動 Enumized<K> の判定は間接継承（interface MyBase : Enumized<K> 経由）も対象とする
        // （docs/エッジケースへの対応方針.md §2）。他の @Enumize 基底は生成された Enumized を持つため除く
        for (superSymbol in resolver.supertypeClosure(symbol)) {
            if (resolver.isEnumizeBase(superSymbol)) continue
            checkManualEnumizedSupertype(symbol, superSymbol, symbol, resolver, context, reporter)
        }
    }

    private fun checkReservedNestedName(
        declaration: FirRegularClass,
        resolver: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val userEnumish =
            declaration.declarations.firstOrNull { nested ->
                nested is FirRegularClass &&
                    nested.name == EnumizeNames.ENUMISH_NAME &&
                    !resolver.isOurGenerated(nested.symbol)
            } ?: return
        reporter.reportOn(userEnumish.source, EnumizeErrors.ENUMIZE_RESERVED_NAME_CLASH, context)
    }

    // 型引数の異なる手動継承 `: Enumized<別の型>`。declaring の直接 supertype を検査し、
    // declaring が報告対象自身でない場合（間接継承）は報告対象側の宣言へ位置づける。
    // 照合は supertype の頭・型引数とも typealias 展開後で行う（別名は同一の型であり、
    // 表記の違いで扱いを変えてはならない。docs/コンパイラプラグイン設計01.md §4・§6.2）
    private fun checkManualEnumizedSupertype(
        reportTarget: FirRegularClassSymbol,
        declaring: FirRegularClassSymbol,
        base: FirRegularClassSymbol,
        resolver: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val expectedArgument = resolver.generatedEnumishClassId(base)
        for (ref in declaring.resolvedSuperTypeRefs) {
            val coneType =
                resolver.tracker.expandedType(ref.coneType) as? ConeClassLikeType ?: continue
            if (coneType.classId != EnumizeNames.ENUMIZED_CLASS_ID) continue
            val argument = coneType.typeArguments.firstOrNull() as? ConeKotlinTypeProjection
            val argumentClassId = argument?.type?.let(resolver.tracker::expandedClassId)
            if (argumentClassId != expectedArgument) {
                val source =
                    if (declaring === reportTarget) ref.source ?: reportTarget.source
                    else reportTarget.source
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

    // 継承者の別ソースセット逸脱に対する補足診断は持たない。別ソースセットの継承者は基底の
    // sealed 継承者一覧に載らず（載らないからこそ言語エラーになる）検査対象になりえないため、
    // コンパイラ本体の診断（sealed の同一モジュール制約）に委ねる（docs/コンパイラプラグイン設計00.md §7・docs/コンパイラプラグイン設計01.md §7.2）

    // ---- 生成 Enumish の直接実装（階層内・kind に限る）----

    // 階層外の実装は sealed の継承者一覧へ反映する経路が無く、JVM では PermittedSubclasses により
    // 実行時拒否になるためコンパイル時にエラーとする（V1-(e) の帰結。docs/コンパイラプラグイン設計00.md §5.2）
    private fun checkManualEnumishImplementation(
        declaration: FirRegularClass,
        membership: EnumizeMembership?,
        resolver: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        for (ref in symbol.resolvedSuperTypeRefs) {
            val superSymbol = resolver.tracker.resolveExpandedClassSymbol(ref.coneType) ?: continue
            // 生成 Enumish の認識は origin だけに依らない構造判定を使う
            // （IC ラウンド外のファイル由来では origin が逆直列化で失われるため）
            if (!resolver.representsGeneratedEnumish(superSymbol)) continue
            val base =
                resolver.tracker.resolveClassSymbol(superSymbol.classId.outerClassId) ?: continue
            if (isLegitimateEnumishImplementor(symbol, membership, base, resolver)) continue
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
    private fun isLegitimateEnumishImplementor(
        symbol: FirRegularClassSymbol,
        membership: EnumizeMembership?,
        base: FirRegularClassSymbol,
        resolver: EnumizeHierarchyResolver,
    ): Boolean {
        if (membership != null && membership.isMemberOf(base.classId)) return true
        if (!symbol.rawStatus.isCompanion) return false
        val outer = resolver.tracker.resolveClassSymbol(symbol.classId.outerClassId) ?: return false
        val outerMembership = resolver.membershipOf(outer) ?: return false
        return outerMembership.isLeaf && outerMembership.isMemberOf(base.classId)
    }

    // ---- 階層メンバー（中間 sealed・末端）に対する検査 ----

    private fun checkHierarchyMember(
        declaration: FirRegularClass,
        membership: EnumizeMembership,
        resolver: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        val base = membership.base
        checkManualEnumizedSupertype(symbol, symbol, base, resolver, context, reporter)
        if (membership.isIntermediate) return
        checkLabelClash(declaration, base, resolver, context, reporter)
        if (declaration.status.isInner) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_INNER_LEAF, context)
            return
        }
        checkManualMemberConflicts(declaration, base, resolver, context, reporter)
        if (symbol.classKind == ClassKind.OBJECT) return
        checkCompanionOfLeafClass(declaration, base, resolver, context, reporter)
    }

    // label 衝突は「検査中の末端が衝突当事者か」を末端ごとに判定し、自分の宣言へ報告する。
    // 診断の座標系は検査中のファイルに紐づくため、基底の検査中に別ファイルの末端の source で
    // 報告してはならない（docs/コンパイラプラグイン設計01.md §7.1 の報告先規則）
    private fun checkLabelClash(
        declaration: FirRegularClass,
        base: FirRegularClassSymbol,
        resolver: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        val others = resolver.leavesSharingLabel(symbol, base)
        if (others.isEmpty()) return
        reporter.reportOn(
            declaration.source,
            EnumizeErrors.ENUMIZE_LABEL_CLASH,
            resolver.labelOf(symbol),
            others.joinToString(separator = ", ") { it.classId.asFqNameString() },
            context,
        )
    }

    // companion は末端 class に必ず存在する（手動宣言か、無ければ候補判定による自動生成。docs/コンパイラプラグイン設計01.md §6.2）。
    // 生成が届かない構成は所属判定ごと成立せず、この検査には到達しない
    private fun checkCompanionOfLeafClass(
        declaration: FirRegularClass,
        base: FirRegularClassSymbol,
        resolver: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        val companion = symbol.companionObjectSymbol ?: return
        if (resolver.isOurGenerated(companion)) return
        if (resolver.membershipOf(companion)?.isLeaf == true) {
            reporter.reportOn(
                companion.source,
                EnumizeErrors.ENUMIZE_COMPANION_LEAF_CONFLICT,
                context,
            )
        }
        val denotable =
            resolver.effectiveVisibilityAtLeast(companion, symbol) ||
                resolver.effectiveVisibilityAtLeast(base, symbol)
        if (!denotable) {
            reporter.reportOn(
                declaration.source,
                EnumizeErrors.ENUMIZE_KIND_TYPE_NOT_DENOTABLE,
                symbol.classId.asFqNameString(),
                context,
            )
        }
    }

    // 生成対象メンバーの手動宣言・階層外 interface からの具象実装の継承（toString は対象外 = docs/コンパイラプラグイン設計01.md §7.2）
    private fun checkManualMemberConflicts(
        declaration: FirRegularClass,
        base: FirRegularClassSymbol,
        resolver: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        val isObjectLeaf = symbol.classKind == ClassKind.OBJECT
        val leafNames =
            if (isObjectLeaf) {
                setOf(
                    EnumizeNames.LABEL,
                    EnumizeNames.ENUMIZED_CLASS_PROPERTY,
                    EnumizeNames.AS_ENUMISH,
                )
            } else {
                setOf(EnumizeNames.AS_ENUMISH)
            }
        reportConflicts(declaration, symbol, leafNames, base, resolver, context, reporter)
        if (isObjectLeaf) return
        val companion = symbol.companionObjectSymbol ?: return
        if (resolver.isOurGenerated(companion)) return
        val kindNames = setOf(EnumizeNames.LABEL, EnumizeNames.ENUMIZED_CLASS_PROPERTY)
        reportConflicts(companion.fir, companion, kindNames, base, resolver, context, reporter)
    }

    private fun reportConflicts(
        declaration: FirRegularClass,
        symbol: FirRegularClassSymbol,
        names: Set<Name>,
        base: FirRegularClassSymbol,
        resolver: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        for (member in manualMembersNamed(declaration, names, resolver)) {
            reporter.reportOn(
                member.source,
                EnumizeErrors.ENUMIZE_MANUAL_MEMBER_CONFLICT,
                memberNameOf(member)?.asString().orEmpty(),
                context,
            )
        }
        for (name in inheritedConcreteConflicts(symbol, names, base, resolver)) {
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
        resolver: EnumizeHierarchyResolver,
    ): List<FirDeclaration> =
        declaration.declarations.filter { member ->
            val name = memberNameOf(member)
            name != null && name in names && !resolver.isOurGeneratedDeclaration(member)
        }

    private fun memberNameOf(declaration: FirDeclaration): Name? =
        when (declaration) {
            is FirNamedFunction -> declaration.name
            is FirProperty -> declaration.name
            else -> null
        }

    // 階層外のユーザー interface から同名メンバーの default 実装（具象）を継承している構成の検出
    private fun inheritedConcreteConflicts(
        symbol: FirRegularClassSymbol,
        names: Set<Name>,
        base: FirRegularClassSymbol,
        resolver: EnumizeHierarchyResolver,
    ): List<Name> {
        val excludedClassIds =
            setOf(
                EnumizeNames.ENUMISH_CLASS_ID,
                EnumizeNames.ENUMISH_COMPANION_CLASS_ID,
                EnumizeNames.ENUMIZED_CLASS_ID,
                resolver.generatedEnumishClassId(base),
                resolver.generatedEnumishCompanionClassId(base),
            )
        val foreignInterfaces =
            resolver.supertypeClosure(symbol).filter { superSymbol ->
                superSymbol.classKind == ClassKind.INTERFACE &&
                    superSymbol.classId !in excludedClassIds &&
                    resolver.basesOf(superSymbol).isEmpty() &&
                    !resolver.isEnumizeBase(superSymbol)
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
                    member.name == name &&
                        (member.getter?.body != null || member.initializer != null)
                else -> false
            }
        }

    // ---- kind の一意対応（AMBIGUOUS_KIND）: 階層内・利用側（プラグイン適用モジュール）の双方 ----

    private fun checkAmbiguousKind(
        declaration: FirRegularClass,
        membership: EnumizeMembership?,
        resolver: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        val kindCandidates = buildList {
            if (membership != null && membership.isLeaf) add(symbol to membership)
            for (superSymbol in resolver.supertypeClosure(symbol)) {
                val superMembership = resolver.membershipOf(superSymbol) ?: continue
                if (superMembership.isLeaf) add(superSymbol to superMembership)
            }
        }
        if (kindCandidates.size < 2) return
        val sameBaseGroup =
            kindCandidates
                .groupBy { (_, candidateMembership) -> candidateMembership.base.classId }
                .entries
                .firstOrNull { (_, group) -> group.size >= 2 } ?: return
        reporter.reportOn(
            declaration.source,
            EnumizeErrors.ENUMIZE_AMBIGUOUS_KIND,
            sameBaseGroup.value[0].first.classId.asFqNameString(),
            sameBaseGroup.value[1].first.classId.asFqNameString(),
            context,
        )
    }

    // ---- 拡張シャドーイング警告 ----

    // label という可視メンバーは、宣言でも継承でも呼び出し点で Enumized<T>.label 拡張を隠す（docs/概要.md §8）。
    // 自クラスの宣言はその位置へ、継承のみの構成は宣言元を添えてクラスの位置へ報告する（§7.1 の報告先規則）
    private fun checkLabelShadowing(
        declaration: FirRegularClass,
        membership: EnumizeMembership?,
        resolver: EnumizeHierarchyResolver,
        context: CheckerContext,
        reporter: DiagnosticReporter,
    ) {
        val symbol = declaration.symbol
        val declared = visibleLabelMembers(declaration, resolver)
        for (member in declared) {
            reporter.reportOn(
                member.source,
                EnumizeErrors.ENUMIZE_EXTENSION_SHADOWED,
                symbol.classId.asFqNameString(),
                context,
            )
        }
        if (declared.isNotEmpty()) return
        // kind（末端 object）には label が生成され、継承した label を必ず override するため実害が無い
        if (membership?.isLeaf == true && symbol.classKind == ClassKind.OBJECT) return
        val owner = inheritedLabelOwner(symbol, resolver) ?: return
        reporter.reportOn(
            declaration.source,
            EnumizeErrors.ENUMIZE_EXTENSION_SHADOWED,
            owner.classId.asFqNameString(),
            context,
        )
    }

    // 継承経路上で label を宣言している最初のクラス。Enumish 由来（runtime-api Enumish・生成 Enumish）の
    // label は kind の値そのものを返すためシャドーイングの実害が無く、対象から外す
    private fun inheritedLabelOwner(
        symbol: FirRegularClassSymbol,
        resolver: EnumizeHierarchyResolver,
    ): FirRegularClassSymbol? =
        resolver.supertypeClosure(symbol).firstOrNull { superSymbol ->
            superSymbol.classId != EnumizeNames.ENUMISH_CLASS_ID &&
                !resolver.representsGeneratedEnumish(superSymbol) &&
                superSymbol.fir.declarations.any { member ->
                    isVisibleLabelMember(member, resolver)
                }
        }

    private fun visibleLabelMembers(
        declaration: FirRegularClass,
        resolver: EnumizeHierarchyResolver,
    ): List<FirDeclaration> =
        declaration.declarations.filter { member -> isVisibleLabelMember(member, resolver) }

    // private メンバーはクラス外の呼び出し点の解決に参加せず継承もされないため、シャドーイングの対象から外す
    private fun isVisibleLabelMember(
        declaration: FirDeclaration,
        resolver: EnumizeHierarchyResolver,
    ): Boolean {
        if (memberNameOf(declaration) != EnumizeNames.LABEL) return false
        if (resolver.isOurGeneratedDeclaration(declaration)) return false
        val visibility =
            when (declaration) {
                is FirNamedFunction -> declaration.status.visibility
                is FirProperty -> declaration.status.visibility
                else -> return false
            }
        return visibility != Visibilities.Private
    }
}
