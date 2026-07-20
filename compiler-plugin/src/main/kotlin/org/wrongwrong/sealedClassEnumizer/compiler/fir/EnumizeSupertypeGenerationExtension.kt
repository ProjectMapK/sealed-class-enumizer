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
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames

// supertype 注入（設計01 §4）:
//   @Enumize 対象（基底）        += Enumized<SI.Enumish>
//   末端 object / data object    += SI.Enumish
//   末端の companion（既存・生成） += SI.Enumish
class EnumizeSupertypeGenerationExtension(session: FirSession) : FirSupertypeGenerationExtension(session) {
    // コンポーネント群の生成順に依存しないよう、初回コールバック時に解決する
    private val resolver: EnumizeHierarchyResolver by lazy { session.enumizeHierarchyResolver }
    private val tracker: EnumizeRawSupertypeTracker get() = resolver.tracker

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(EnumizePredicates.ENUMIZE)
    }

    // 判定材料は raw な情報に限る（このコールバックは supertype 解決前に呼ばれうる）。
    // companion は「外側が末端（kind を担う）」と「companion 自身が末端（階層外クラスの companion が
    // 単独で末端になる許容構成 — 設計01 §7.2 COMPANION_LEAF_CONFLICT の注記）」の両方を candidate にする。
    // typealias 経由の supertype は raw 段階では展開できないため、ゲートだけ保守的に開き、
    // 実際の階層判定は computeAdditionalSupertypes の解決済み ref で行う
    override fun needTransformSupertypes(declaration: FirClassLikeDeclaration): Boolean {
        val regularClass = declaration as? FirRegularClass ?: return false
        if (regularClass.symbol.isLocal) return false
        return when {
            session.predicateBasedProvider.matches(EnumizePredicates.ENUMIZE, regularClass) -> true
            regularClass.classKind != ClassKind.OBJECT -> false
            !regularClass.status.isCompanion -> couldBeHierarchyMember(regularClass.symbol)
            else -> couldBeHierarchyMember(regularClass.symbol) ||
                outerSymbolOf(regularClass)?.let(::couldBeHierarchyMember) == true
        }
    }

    private fun couldBeHierarchyMember(symbol: FirRegularClassSymbol): Boolean =
        tracker.isHierarchyCandidate(symbol, followTypeAliases = true)

    override fun computeAdditionalSupertypes(
        classLikeDeclaration: FirClassLikeDeclaration,
        resolvedSupertypes: List<FirResolvedTypeRef>,
        typeResolver: TypeResolveService,
    ): List<ConeKotlinType> {
        val regularClass = classLikeDeclaration as? FirRegularClass ?: return emptyList()
        return when {
            session.predicateBasedProvider.matches(EnumizePredicates.ENUMIZE, regularClass) ->
                supertypesForBase(regularClass, resolvedSupertypes)
            regularClass.status.isCompanion -> supertypesForCompanion(regularClass, resolvedSupertypes)
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
        // 手動の Enumized<K> がある場合（間接継承経由を含む — エッジ §2）は注入しない。型引数一致なら
        // 重複回避、不一致なら ENUMIZE_MANUAL_SUPERTYPE_MISMATCH をチェッカーが報告する（設計01 §4）
        if (hasEnumizedSupertype(resolvedSupertypes)) return emptyList()
        val enumishType = base.symbol.classId
            .createNestedClassId(EnumizeNames.ENUMISH_NAME)
            .constructClassLikeType()
        return listOf(
            EnumizeNames.ENUMIZED_CLASS_ID.constructClassLikeType(arrayOf<ConeTypeProjection>(enumishType))
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
        val base = tracker.findEnumizeBase(outer, followTypeAliases = true) ?: return emptyList()
        return enumishInjectionFor(base, resolvedSupertypes)
    }

    // 外側（末端）の supertype 解決は companion より先に走るため、解決済み ref を tracker 経由で辿れる
    private fun findBaseFromResolved(resolvedSupertypes: List<FirResolvedTypeRef>): FirRegularClassSymbol? {
        val superSymbols = resolvedSupertypes.mapNotNull { tracker.resolveExpandedClassSymbol(it.coneType) }
        return tracker.findEnumizeBaseAmong(superSymbols, followTypeAliases = true)
    }

    private fun enumishInjectionFor(
        base: FirRegularClassSymbol,
        resolvedSupertypes: List<FirResolvedTypeRef>,
    ): List<ConeKotlinType> {
        if (resolver.hasUserDeclaredNestedEnumish(base)) return emptyList()
        val enumishClassId = base.classId.createNestedClassId(EnumizeNames.ENUMISH_NAME)
        // 型引数まで一致する手動宣言（この場合は非ジェネリックなので ClassId 一致）があればスキップ
        if (resolvedSupertypes.any { it.coneType.classId == enumishClassId }) return emptyList()
        return listOf(enumishClassId.constructClassLikeType())
    }

    private fun hasEnumizedSupertype(resolvedSupertypes: List<FirResolvedTypeRef>): Boolean =
        resolvedSupertypes.any { ref ->
            ref.coneType.classId == EnumizeNames.ENUMIZED_CLASS_ID ||
                tracker.resolveExpandedClassSymbol(ref.coneType)?.let { superSymbol ->
                    tracker.reachesSupertype(superSymbol, EnumizeNames.ENUMIZED_CLASS_ID, followTypeAliases = true)
                } == true
        }

    private fun outerSymbolOf(regularClass: FirRegularClass): FirRegularClassSymbol? =
        tracker.resolveClassSymbol(regularClass.symbol.classId.outerClassId)
}
