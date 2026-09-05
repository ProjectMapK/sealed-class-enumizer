@file:OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)

package io.github.projectmapk.sealedClassEnumizer.compiler.fir.checkers

import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeNames
import io.github.projectmapk.sealedClassEnumizer.compiler.fir.EnumizeHierarchyResolver
import io.github.projectmapk.sealedClassEnumizer.compiler.fir.EnumizeMembership
import io.github.projectmapk.sealedClassEnumizer.compiler.fir.EnumizePredicates
import io.github.projectmapk.sealedClassEnumizer.compiler.fir.callableNameOrNull
import io.github.projectmapk.sealedClassEnumizer.compiler.fir.enumizeHierarchyResolver
import io.github.projectmapk.sealedClassEnumizer.compiler.fir.isGeneratedByEnumize
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
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
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinTypeProjection
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.renderReadable
import org.jetbrains.kotlin.name.Name

// 診断カタログの検査ロジック（docs/コンパイラプラグイン設計01.md §7.2）。マルチラウンド IC の部分集合ビューで偽陽性を出さないよう、
// すべての検査を「見えている宣言の性質に対する条件検査」として実装する（単調性。docs/コンパイラプラグイン設計01.md §7.1）。
// 検査対象クラスの所属（EnumizeMembership）は入口で一度だけ resolver から読み、各検査へ引数で受け渡す。
// 個々の検査は階層照会コンポーネントを受け手に取り、報告先の文脈はコンテキストパラメータで受ける
object EnumizeRegularClassChecker : FirRegularClassChecker(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirRegularClass) {
        val symbol = declaration.symbol
        if (symbol.isLocal) return
        val annotated =
            context.session.predicateBasedProvider.matches(EnumizePredicates.ENUMIZE, declaration)
        with(context.session.enumizeHierarchyResolver) {
            // typealias / import 別名表記の @Enumize は述語（エイリアス展開前に確定）に載らず生成が
            // 走らない。CHECKERS では解決済みアノテーションとして観測できるため、この差分をエラーにする
            // （静かな非生成を許さない。docs/コンパイラプラグイン設計01.md §7.2）
            if (!annotated && tracker.hasResolvedEnumizeAnnotation(symbol)) {
                reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_ALIASED_ANNOTATION)
            }
            // 階層の一意性を見る診断だけは異常状態（非所属・複数所属）の内訳が要るため一覧を生読みする。
            // 以降の検査へ取り回すのは正常な所属（membership。異常時は null）のみ
            val belongingBases = basesOf(symbol)
            val membership = membershipOf(symbol)
            if (annotated) {
                checkBase(declaration)
                if (belongingBases.isNotEmpty()) {
                    reporter.reportOn(
                        declaration.source,
                        EnumizeErrors.ENUMIZE_NESTED_IN_HIERARCHY,
                        belongingBases.first().classId.asFqNameString(),
                    )
                }
            }
            if (belongingBases.size >= 2) {
                reporter.reportOn(
                    declaration.source,
                    EnumizeErrors.ENUMIZE_MULTIPLE_HIERARCHIES,
                    belongingBases[0].classId.asFqNameString(),
                    belongingBases[1].classId.asFqNameString(),
                )
            }
            if (annotated || belongingBases.isNotEmpty()) {
                checkLabelShadowing(declaration, membership)
            }
            checkAmbiguousKind(declaration, membership)
            checkManualEnumishImplementation(declaration, membership)
            if (membership != null) {
                checkHierarchyMember(declaration, membership)
            }
        }
    }

    // ---- @Enumize 対象（基底）に対する検査 ----

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun EnumizeHierarchyResolver.checkBase(declaration: FirRegularClass) {
        val symbol = declaration.symbol
        if (!isSealed(symbol)) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_NOT_SEALED)
            return
        }
        if (declaration.isExpect) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_ON_EXPECT)
        }
        if (declaration.isActual) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_ON_ACTUAL)
        }
        checkReservedNestedName(declaration)
        checkManualEnumizedSupertype(symbol, symbol, symbol)
        // 手動 Enumized<K> の判定は間接継承（interface MyBase : Enumized<K> 経由）も対象とする
        // （docs/エッジケースへの対応方針.md §2）。他の @Enumize 基底は生成された Enumized を持つため除く
        for (superSymbol in supertypeClosure(symbol)) {
            if (isEnumizeBase(superSymbol)) continue
            checkManualEnumizedSupertype(symbol, superSymbol, symbol)
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun EnumizeHierarchyResolver.checkReservedNestedName(declaration: FirRegularClass) {
        val userEnumish =
            declaration.declarations.firstOrNull { nested ->
                nested is FirRegularClass &&
                    nested.name == EnumizeNames.ENUMISH_NAME &&
                    !nested.symbol.isGeneratedByEnumize
            } ?: return
        reporter.reportOn(userEnumish.source, EnumizeErrors.ENUMIZE_RESERVED_NAME_CLASH)
    }

    // 型引数の異なる手動継承 : Enumized<別の型>。declaring の直接 supertype を検査し、
    // declaring が報告対象自身でない場合（間接継承）は報告対象側の宣言へ位置づける。
    // 照合は supertype の頭・型引数とも typealias 展開後で行う（別名は同一の型であり、
    // 表記の違いで扱いを変えてはならない。docs/コンパイラプラグイン設計01.md §4・§6.2）
    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun EnumizeHierarchyResolver.checkManualEnumizedSupertype(
        reportTarget: FirRegularClassSymbol,
        declaring: FirRegularClassSymbol,
        base: FirRegularClassSymbol,
    ) {
        val expectedArgument = generatedEnumishClassId(base)
        for (ref in declaring.resolvedSuperTypeRefs) {
            val coneType = tracker.expandedType(ref.coneType) as? ConeClassLikeType ?: continue
            if (coneType.classId != EnumizeNames.ENUMIZED_CLASS_ID) continue
            val argument = coneType.typeArguments.firstOrNull() as? ConeKotlinTypeProjection
            val argumentClassId = argument?.type?.let(tracker::expandedClassId)
            if (argumentClassId != expectedArgument) {
                val source =
                    if (declaring === reportTarget) ref.source ?: reportTarget.source
                    else reportTarget.source
                reporter.reportOn(
                    source,
                    EnumizeErrors.ENUMIZE_MANUAL_SUPERTYPE_MISMATCH,
                    coneType.renderReadable(),
                    "Enumized<${expectedArgument.asFqNameString()}>",
                )
            }
        }
    }

    // ---- 生成 Enumish の直接実装（階層内・kind に限る）----

    // 階層外の実装は sealed の継承者一覧へ反映する経路が無く、JVM では PermittedSubclasses により
    // 実行時拒否になるためコンパイル時にエラーとする（V1-(e) の帰結。docs/コンパイラプラグイン設計00.md §5.2）
    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun EnumizeHierarchyResolver.checkManualEnumishImplementation(
        declaration: FirRegularClass,
        membership: EnumizeMembership?,
    ) {
        val symbol = declaration.symbol
        for (ref in symbol.resolvedSuperTypeRefs) {
            val superSymbol = tracker.resolveExpandedClassSymbol(ref.coneType) ?: continue
            // 生成 Enumish の認識は origin だけに依らない構造判定を使う
            // （IC ラウンド外のファイル由来では origin が逆直列化で失われるため）
            if (!representsGeneratedEnumish(superSymbol)) continue
            val base = tracker.resolveClassSymbol(superSymbol.classId.outerClassId) ?: continue
            if (isLegitimateEnumishImplementor(symbol, membership, base)) continue
            reporter.reportOn(
                ref.source ?: declaration.source,
                EnumizeErrors.ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY,
                base.classId.asFqNameString(),
            )
        }
    }

    // 階層のメンバー（末端 class 自身による実装を含む）と、階層の末端の kind を担う companion は
    // 生成 Enumish の正当な実装である
    private fun EnumizeHierarchyResolver.isLegitimateEnumishImplementor(
        symbol: FirRegularClassSymbol,
        membership: EnumizeMembership?,
        base: FirRegularClassSymbol,
    ): Boolean {
        if (membership != null && membership.isMemberOf(base.classId)) return true
        if (!symbol.rawStatus.isCompanion) return false
        val outer = tracker.resolveClassSymbol(symbol.classId.outerClassId) ?: return false
        val outerMembership = membershipOf(outer) ?: return false
        return outerMembership.isLeaf && outerMembership.isMemberOf(base.classId)
    }

    // ---- 階層メンバー（中間 sealed・末端）に対する検査 ----

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun EnumizeHierarchyResolver.checkHierarchyMember(
        declaration: FirRegularClass,
        membership: EnumizeMembership,
    ) {
        val symbol = declaration.symbol
        val base = membership.base
        checkManualEnumizedSupertype(symbol, symbol, base)
        if (!membership.isLeaf) return
        checkLabelClash(declaration, base)
        if (declaration.status.isInner) {
            reporter.reportOn(declaration.source, EnumizeErrors.ENUMIZE_INNER_LEAF)
            return
        }
        checkMemberConflicts(declaration, base)
        if (symbol.classKind == ClassKind.OBJECT) return
        checkCompanionOfLeafClass(declaration, base)
    }

    // label 衝突は「検査中の末端が衝突当事者か」を末端ごとに判定し、自分の宣言へ報告する。
    // 診断の座標系は検査中のファイルに紐づくため、基底の検査中に別ファイルの末端の source で
    // 報告してはならない（docs/コンパイラプラグイン設計01.md §7.1 の報告先規則）
    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun EnumizeHierarchyResolver.checkLabelClash(
        declaration: FirRegularClass,
        base: FirRegularClassSymbol,
    ) {
        val symbol = declaration.symbol
        val others = leavesSharingLabel(symbol, base)
        if (others.isEmpty()) return
        reporter.reportOn(
            declaration.source,
            EnumizeErrors.ENUMIZE_LABEL_CLASH,
            labelOf(symbol, base),
            others.joinToString(separator = ", ") { it.classId.asFqNameString() },
        )
    }

    // companion は末端 class に必ず存在する（手動宣言か、無ければ候補判定による自動生成。docs/コンパイラプラグイン設計01.md §6.2）。
    // 生成が届かない構成は所属判定ごと成立せず、この検査には到達しない。
    // companion 自身の末端判定は階層不問で行う（別階層の末端でも同一オブジェクトが 2 つの末端の kind になる。
    // docs/コンパイラプラグイン設計01.md §7.2）
    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun EnumizeHierarchyResolver.checkCompanionOfLeafClass(
        declaration: FirRegularClass,
        base: FirRegularClassSymbol,
    ) {
        val symbol = declaration.symbol
        val companion = symbol.companionObjectSymbol ?: return
        if (companion.isGeneratedByEnumize) return
        if (membershipOf(companion)?.isLeaf == true) {
            reporter.reportOn(companion.source, EnumizeErrors.ENUMIZE_COMPANION_LEAF_CONFLICT)
        }
        val denotable =
            effectiveVisibilityAtLeast(companion, symbol) ||
                effectiveVisibilityAtLeast(base, symbol)
        if (!denotable) {
            reporter.reportOn(
                declaration.source,
                EnumizeErrors.ENUMIZE_KIND_TYPE_NOT_DENOTABLE,
                symbol.classId.asFqNameString(),
            )
        }
    }

    // 生成対象メンバーの手動宣言・階層外 interface からの具象 default 実装の継承・
    // クラス supertype からの final 具象の継承（toString は対象外 = docs/コンパイラプラグイン設計01.md §7.2）
    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun EnumizeHierarchyResolver.checkMemberConflicts(
        declaration: FirRegularClass,
        base: FirRegularClassSymbol,
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
        reportConflicts(declaration, symbol, leafNames, base)
        if (isObjectLeaf) return
        val companion = symbol.companionObjectSymbol ?: return
        if (companion.isGeneratedByEnumize) return
        val kindNames = setOf(EnumizeNames.LABEL, EnumizeNames.ENUMIZED_CLASS_PROPERTY)
        reportConflicts(companion.fir, companion, kindNames, base)
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun EnumizeHierarchyResolver.reportConflicts(
        declaration: FirRegularClass,
        symbol: FirRegularClassSymbol,
        names: Set<Name>,
        base: FirRegularClassSymbol,
    ) {
        for (member in manualMembersNamed(declaration, names)) {
            reporter.reportOn(
                member.source,
                EnumizeErrors.ENUMIZE_MEMBER_CONFLICT,
                member.callableNameOrNull?.asString().orEmpty(),
            )
        }
        val inheritedConflicts =
            (inheritedConcreteConflicts(symbol, names, base) +
                    inheritedFinalClassConflicts(symbol, names))
                .distinct()
        for (name in inheritedConflicts) {
            reporter.reportOn(
                declaration.source,
                EnumizeErrors.ENUMIZE_MEMBER_CONFLICT,
                name.asString(),
            )
        }
    }

    private fun EnumizeHierarchyResolver.manualMembersNamed(
        declaration: FirRegularClass,
        names: Set<Name>,
    ): List<FirDeclaration> =
        declaration.declarations.filter { member ->
            val name = member.callableNameOrNull
            name != null && name in names && !member.symbol.isGeneratedByEnumize
        }

    // 階層外のユーザー interface から同名メンバーの default 実装（具象）を継承している構成の検出
    private fun EnumizeHierarchyResolver.inheritedConcreteConflicts(
        symbol: FirRegularClassSymbol,
        names: Set<Name>,
        base: FirRegularClassSymbol,
    ): List<Name> {
        val excludedClassIds =
            setOf(
                EnumizeNames.ENUMISH_CLASS_ID,
                EnumizeNames.ENUMISH_COMPANION_CLASS_ID,
                EnumizeNames.ENUMIZED_CLASS_ID,
                generatedEnumishClassId(base),
                generatedEnumishCompanionClassId(base),
            )
        val foreignInterfaces =
            supertypeClosure(symbol).filter { superSymbol ->
                superSymbol.classKind == ClassKind.INTERFACE &&
                    superSymbol.classId !in excludedClassIds &&
                    basesOf(superSymbol).isEmpty() &&
                    !isEnumizeBase(superSymbol)
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

    // クラス supertype（階層内外を問わない）から継承する final 具象メンバーの検出。
    // 生成 override が final メンバーを踏む構成は FE 診断の無いままコンパイルを通過し、
    // 実行時のクラスロードで IncompatibleClassChangeError になるため、コンパイル時にエラーとする。
    // 対象は JVM シグネチャが実際に衝突する宣言種別のみ（label / enumizedClass はプロパティ・
    // asEnumish は引数なし関数。種別交差は衝突せず成立するため対象外）
    private fun EnumizeHierarchyResolver.inheritedFinalClassConflicts(
        symbol: FirRegularClassSymbol,
        names: Set<Name>,
    ): List<Name> {
        val classSupertypes = supertypeClosure(symbol).filter { it.classKind == ClassKind.CLASS }
        return names.filter { name ->
            classSupertypes.any { superClass -> declaresFinalConflictingMember(superClass, name) }
        }
    }

    private fun declaresFinalConflictingMember(symbol: FirRegularClassSymbol, name: Name): Boolean =
        symbol.fir.declarations.any { member -> isFinalConflictingMember(member, name) }

    private fun isFinalConflictingMember(member: FirDeclaration, name: Name): Boolean =
        if (name == EnumizeNames.AS_ENUMISH) {
            member is FirNamedFunction &&
                member.name == name &&
                member.valueParameters.isEmpty() &&
                member.receiverParameter == null &&
                isFinalNonPrivateMember(member.symbol)
        } else {
            member is FirProperty && member.name == name && isFinalNonPrivateMember(member.symbol)
        }

    // private はサブクラスの override 解決に参加しないため衝突しない（final でも生成側が独立に成立する）
    private fun isFinalNonPrivateMember(symbol: FirCallableSymbol<*>): Boolean {
        val status = symbol.resolvedStatus
        return status.modality == Modality.FINAL && status.visibility != Visibilities.Private
    }

    // ---- kind の一意対応（AMBIGUOUS_KIND）: 階層内・利用側（プラグイン適用モジュール）の双方 ----

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun EnumizeHierarchyResolver.checkAmbiguousKind(
        declaration: FirRegularClass,
        membership: EnumizeMembership?,
    ) {
        val symbol = declaration.symbol
        val kindCandidates = buildList {
            if (membership != null && membership.isLeaf) add(symbol to membership)
            for (superSymbol in supertypeClosure(symbol)) {
                val superMembership = membershipOf(superSymbol) ?: continue
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
        )
    }

    // ---- 拡張シャドーイング警告 ----

    // label という可視プロパティは、宣言でも継承でも呼び出し点で Enumized<T>.label 拡張を隠す（docs/概要.md §8）。
    // 同名の関数はプロパティ参照の候補にならず拡張を隠さないため対象外である。
    // 自クラスの宣言はその位置へ、継承のみの構成は宣言元を添えてクラスの位置へ報告する（docs/コンパイラプラグイン設計01.md §7.1 の報告先規則）
    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun EnumizeHierarchyResolver.checkLabelShadowing(
        declaration: FirRegularClass,
        membership: EnumizeMembership?,
    ) {
        val symbol = declaration.symbol
        val declared = declaration.declarations.filter { isVisibleLabelProperty(it) }
        for (member in declared) {
            reporter.reportOn(
                member.source,
                EnumizeErrors.ENUMIZE_EXTENSION_SHADOWED,
                symbol.classId.asFqNameString(),
            )
        }
        if (declared.isNotEmpty()) return
        // kind（末端 object）には label が生成され、継承した label を必ず override するため実害が無い
        if (membership?.isLeaf == true && symbol.classKind == ClassKind.OBJECT) return
        val owner = inheritedLabelOwner(symbol) ?: return
        reporter.reportOn(
            declaration.source,
            EnumizeErrors.ENUMIZE_EXTENSION_SHADOWED,
            owner.classId.asFqNameString(),
        )
    }

    // 継承経路上で label を宣言している最初のクラス。Enumish 由来（runtime-api Enumish・生成 Enumish）の
    // label は kind の値そのものを返すためシャドーイングの実害が無く、対象から外す
    private fun EnumizeHierarchyResolver.inheritedLabelOwner(
        symbol: FirRegularClassSymbol
    ): FirRegularClassSymbol? =
        supertypeClosure(symbol).firstOrNull { superSymbol ->
            superSymbol.classId != EnumizeNames.ENUMISH_CLASS_ID &&
                !representsGeneratedEnumish(superSymbol) &&
                superSymbol.fir.declarations.any { isVisibleLabelProperty(it) }
        }

    // private プロパティはクラス外の呼び出し点の解決に参加せず継承もされないため、シャドーイングの対象から外す
    private fun EnumizeHierarchyResolver.isVisibleLabelProperty(
        declaration: FirDeclaration
    ): Boolean {
        if (declaration !is FirProperty || declaration.name != EnumizeNames.LABEL) return false
        if (declaration.symbol.isGeneratedByEnumize) return false
        return declaration.status.visibility != Visibilities.Private
    }
}
