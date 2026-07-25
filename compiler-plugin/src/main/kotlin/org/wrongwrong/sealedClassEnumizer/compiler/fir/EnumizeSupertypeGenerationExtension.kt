@file:OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)

package org.wrongwrong.sealedClassEnumizer.compiler.fir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.FirSupertypeGenerationExtension
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeProjection
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.name.ClassId
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames

// supertype 注入（docs/コンパイラプラグイン設計01.md §4）:
//   @Enumize 対象（基底）        += Enumized<SI.Enumish>
//   末端 object / data object    += SI.Enumish
//   末端の既存 companion         += SI.Enumish
// プラグイン生成の companion は本拡張が訪問しないため対象外であり、supertype は生成時に直接指定する（docs/コンパイラプラグイン設計01.md §5.1）
class EnumizeSupertypeGenerationExtension(session: FirSession) :
    FirSupertypeGenerationExtension(session) {
    // コンポーネント群の生成順に依存しないよう、初回コールバック時に解決する
    private val resolver: EnumizeHierarchyResolver by lazy { session.enumizeHierarchyResolver }
    private val tracker: EnumizeRawSupertypeTracker
        get() = resolver.tracker

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(EnumizePredicates.ENUMIZE)
    }

    // 判定材料は raw な情報に限る（このコールバックは supertype 解決前に呼ばれうる）。
    // companion は「外側が末端（kind を担う）」と「companion 自身が末端（階層外クラスの companion が
    // 単独で末端になる許容構成 — docs/コンパイラプラグイン設計01.md §7.2 COMPANION_LEAF_CONFLICT の注記）」の両方を candidate にする。
    // supertype の表記（typealias・import エイリアス等）は raw 追跡が展開して候補を拾い、
    // 実際の階層判定は computeAdditionalSupertypes の解決済み ref を展開して行う
    override fun needTransformSupertypes(declaration: FirClassLikeDeclaration): Boolean {
        val regularClass = declaration as? FirRegularClass ?: return false
        if (regularClass.symbol.isLocal) return false
        return when {
            session.predicateBasedProvider.matches(EnumizePredicates.ENUMIZE, regularClass) -> true
            regularClass.classKind != ClassKind.OBJECT -> false
            !regularClass.status.isCompanion -> couldBeHierarchyMember(regularClass.symbol)
            else ->
                couldBeHierarchyMember(regularClass.symbol) ||
                    outerSymbolOf(regularClass)?.let(::couldBeHierarchyMember) == true
        }
    }

    private fun couldBeHierarchyMember(symbol: FirRegularClassSymbol): Boolean =
        tracker.isHierarchyCandidate(symbol)

    override fun computeAdditionalSupertypes(
        classLikeDeclaration: FirClassLikeDeclaration,
        resolvedSupertypes: List<FirResolvedTypeRef>,
        typeResolver: TypeResolveService,
    ): List<ConeKotlinType> {
        val regularClass = classLikeDeclaration as? FirRegularClass ?: return emptyList()
        return when {
            session.predicateBasedProvider.matches(EnumizePredicates.ENUMIZE, regularClass) ->
                supertypesForBase(regularClass, resolvedSupertypes)
            regularClass.status.isCompanion ->
                supertypesForCompanion(regularClass, resolvedSupertypes)
            else -> supertypesForLeafObject(regularClass, resolvedSupertypes)
        }
    }

    // companion 自身が末端である場合（自身の supertype が階層に到達する場合）は末端 object として扱い、
    // そうでなければ外側の末端の kind として扱う
    private fun supertypesForCompanion(
        companion: FirRegularClass,
        resolvedSupertypes: List<FirResolvedTypeRef>,
    ): List<ConeKotlinType> {
        val asLeafObject = supertypesForLeafObject(companion, resolvedSupertypes)
        if (asLeafObject.isNotEmpty()) return asLeafObject
        return supertypesForKindCompanion(companion, resolvedSupertypes)
    }

    private fun supertypesForBase(
        base: FirRegularClass,
        resolvedSupertypes: List<FirResolvedTypeRef>,
    ): List<ConeKotlinType> {
        // 既存のネスト宣言 Enumish がある場合は生成自体をスキップするため注入もしない（ENUMIZE_RESERVED_NAME_CLASH）
        if (resolver.hasUserDeclaredNestedEnumish(base.symbol)) return emptyList()
        // 手動の Enumized<K> がある場合（間接継承経由を含む — docs/エッジケースへの対応方針.md §2）は注入しない。型引数一致なら
        // 重複回避、不一致なら ENUMIZE_MANUAL_SUPERTYPE_MISMATCH をチェッカーが報告する（docs/コンパイラプラグイン設計01.md §4）
        if (hasEnumizedSupertype(resolvedSupertypes)) return emptyList()
        val enumishType =
            base.symbol.classId
                .createNestedClassId(EnumizeNames.ENUMISH_NAME)
                .constructClassLikeType()
        return listOf(
            EnumizeNames.ENUMIZED_CLASS_ID.constructClassLikeType(
                arrayOf<ConeTypeProjection>(enumishType)
            )
        )
    }

    private fun supertypesForLeafObject(
        leafObject: FirRegularClass,
        resolvedSupertypes: List<FirResolvedTypeRef>,
    ): List<ConeKotlinType> {
        if (leafObject.classKind != ClassKind.OBJECT) return emptyList()
        val base = findBaseFromResolved(resolvedSupertypes) ?: return emptyList()
        return enumishInjectionFor(base, resolvedSupertypes)
    }

    private fun supertypesForKindCompanion(
        companion: FirRegularClass,
        resolvedSupertypes: List<FirResolvedTypeRef>,
    ): List<ConeKotlinType> {
        val outer = outerSymbolOf(companion) ?: return emptyList()
        // 中間 sealed の companion は kind ではない。外側が object の場合 companion は存在しない
        if (tracker.isRawSealed(outer) || outer.classKind == ClassKind.OBJECT) return emptyList()
        val base = tracker.findEnumizeBase(outer) ?: return emptyList()
        return enumishInjectionFor(base, resolvedSupertypes)
    }

    // 処理中の宣言に与えられた解決済み supertype を展開して辿り、階層の基底を探す
    private fun findBaseFromResolved(
        resolvedSupertypes: List<FirResolvedTypeRef>
    ): FirRegularClassSymbol? {
        val superSymbols = resolvedSupertypes.mapNotNull {
            tracker.resolveExpandedClassSymbol(it.coneType)
        }
        return tracker.findEnumizeBaseAmong(superSymbols)
    }

    private fun enumishInjectionFor(
        base: FirRegularClassSymbol,
        resolvedSupertypes: List<FirResolvedTypeRef>,
    ): List<ConeKotlinType> {
        if (resolver.hasUserDeclaredNestedEnumish(base)) return emptyList()
        val enumishClassId = base.classId.createNestedClassId(EnumizeNames.ENUMISH_NAME)
        // 型引数まで一致する手動宣言（この場合は非ジェネリックなので ClassId 一致）があればスキップ
        if (resolvedSupertypes.any { expandedClassIdOf(it) == enumishClassId }) return emptyList()
        return listOf(enumishClassId.constructClassLikeType())
    }

    private fun hasEnumizedSupertype(resolvedSupertypes: List<FirResolvedTypeRef>): Boolean =
        resolvedSupertypes.any(::declaresOrInheritsEnumized)

    // 展開後が Enumized 自身である場合を先に判定し、そうでなければ間接継承
    // （interface MyBase : Enumized<K> 経由 — docs/エッジケースへの対応方針.md §2）を supertype グラフで探す
    private fun declaresOrInheritsEnumized(ref: FirResolvedTypeRef): Boolean {
        val classId = expandedClassIdOf(ref) ?: return false
        if (classId == EnumizeNames.ENUMIZED_CLASS_ID) return true
        val superSymbol = tracker.resolveClassSymbol(classId) ?: return false
        return tracker.reachesSupertype(superSymbol, EnumizeNames.ENUMIZED_CLASS_ID)
    }

    // このコールバックが受け取る解決済み型は typealias が未展開のことがあるため、照合の前に展開する
    // （型の同一性は表記に依らない — docs/コンパイラプラグイン設計01.md §4・§6.2）
    private fun expandedClassIdOf(ref: FirResolvedTypeRef): ClassId? =
        tracker.expandedClassId(ref.coneType)

    private fun outerSymbolOf(regularClass: FirRegularClass): FirRegularClassSymbol? =
        tracker.resolveClassSymbol(regularClass.symbol.classId.outerClassId)
}
