@file:OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)

package org.wrongwrong.sealedClassEnumizer.compiler.fir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.createCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.caches.getValue
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.getSealedClassInheritors
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent
import org.jetbrains.kotlin.fir.extensions.extensionService
import org.jetbrains.kotlin.fir.extensions.extensionSessionComponents
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeStarProjection
import org.jetbrains.kotlin.fir.types.ConeTypeProjection
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeKey
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames

// 階層照会のセッション単一コンポーネント。所属事実（EnumizeMembership）は membershipOf がクラス毎に
// 一度だけ計算してキャッシュし、消費側（宣言生成の役割判定・チェッカー）はその値を受け渡して読む。
// 所属の計算（supertype の追跡）は tracker が担い、それ以外の照会は解決済み情報
// （sealed inheritors 属性・解決済み supertype・実効可視性）を用いる。
// membershipOf は SUPER_TYPES 中（getCallableNamesForClass 経由。docs/コンパイラプラグイン設計01.md §3）にも呼ばれ、
// そのクラス自身の supertype ref が未解決のまま答えが確定するため、tracker は raw な ref からでも
// 解決後と同じ答えを返せる必要がある（docs/コンパイラプラグイン設計01.md §6.1）。
// 用語（階層・末端・中間 sealed・kind）はdocs/コンパイラプラグイン設計00.md §1 に従う。
class EnumizeHierarchyResolver(session: FirSession) : FirExtensionSessionComponent(session) {
    val tracker: EnumizeRawSupertypeTracker = EnumizeRawSupertypeTracker(session)

    private val basesCache: FirCache<FirRegularClassSymbol, List<FirRegularClassSymbol>, Nothing?> =
        session.firCachesFactory.createCache { symbol -> computeBases(symbol) }

    // 階層ごとの label 索引（label → その label を持つ末端）。LABEL_CLASH の検査は末端の数だけ走るため、
    // 階層 1 つにつき一度だけ構築する
    private val labelIndexCache:
        FirCache<FirRegularClassSymbol, Map<String, List<FirRegularClassSymbol>>, Nothing?> =
        session.firCachesFactory.createCache { base -> leavesOf(base).groupBy(::labelOf) }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(EnumizePredicates.ENUMIZE)
    }

    // 階層への正常な所属（取り回しの唯一の入口）。ちょうど 1 つの @Enumize 基底に属する場合のみ
    // 非 null を返し、非所属・複数階層への所属（エラー構成）は null で表現する
    fun membershipOf(symbol: FirRegularClassSymbol): EnumizeMembership? {
        val base = basesCache.getValue(symbol).singleOrNull() ?: return null
        return EnumizeMembership(base, tracker.isRawSealed(symbol))
    }

    // 所属する基底の一覧の生読み。階層の一意性を見る診断（MULTIPLE_FAMILIES / NESTED_IN_HIERARCHY）と
    // 「どの階層にも属さない」ことの判定にのみ使い、通常の取り回しには membershipOf を使う
    fun basesOf(symbol: FirRegularClassSymbol): List<FirRegularClassSymbol> =
        basesCache.getValue(symbol)

    fun isEnumizeBase(symbol: FirRegularClassSymbol): Boolean =
        tracker.isEnumizeBase(symbol) && tracker.isRawSealed(symbol)

    fun isSealed(symbol: FirRegularClassSymbol): Boolean = tracker.isRawSealed(symbol)

    fun isOurGenerated(symbol: FirBasedSymbol<*>): Boolean =
        (symbol.origin as? FirDeclarationOrigin.Plugin)?.key == EnumizeKey

    // このシンボルが生成 Enumish（SI.Enumish）を表すか。同一 IC ラウンドの生成物は origin で判定できるが、
    // ラウンド外のファイル由来は前ラウンドのメタデータからの逆直列化で origin が失われるため、
    // 「@Enumize 付き基底の直下の Enumish」という構造で判定する（ネスト名 Enumish の手動宣言は
    // RESERVED_NAME_CLASH でコンパイル不能のため、有効な前ラウンド出力とは衝突しない）
    fun representsGeneratedEnumish(symbol: FirRegularClassSymbol): Boolean {
        if (symbol.classId.shortClassName != EnumizeNames.ENUMISH_NAME) return false
        if (isOurGenerated(symbol)) return true
        val outer = tracker.resolveClassSymbol(symbol.classId.outerClassId) ?: return false
        return isEnumizeBase(outer)
    }

    fun isOurGeneratedDeclaration(declaration: FirDeclaration): Boolean =
        (declaration.origin as? FirDeclarationOrigin.Plugin)?.key == EnumizeKey

    fun generatedEnumishClassId(base: FirRegularClassSymbol): ClassId =
        base.classId.createNestedClassId(EnumizeNames.ENUMISH_NAME)

    fun generatedEnumishCompanionClassId(base: FirRegularClassSymbol): ClassId =
        generatedEnumishClassId(base)
            .createNestedClassId(SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT)

    // 基底の sealed inheritors 属性を再帰展開した階層の全メンバー（中間 sealed を含む・基底自身を除く）。
    // 並べ替えは行わず、コンパイラが提供する継承者リストの走査順のまま返す（docs/コンパイラプラグイン設計00.md §6.2）
    fun hierarchyMembersOf(base: FirRegularClassSymbol): List<FirRegularClassSymbol> {
        val result = LinkedHashMap<ClassId, FirRegularClassSymbol>()
        collectMembers(base, LinkedHashSet(), result)
        return result.values.toList()
    }

    // 階層の末端のみ（中間 sealed の位置にその継承者が入れ子展開された順序）
    fun leavesOf(base: FirRegularClassSymbol): List<FirRegularClassSymbol> =
        hierarchyMembersOf(base).filterNot { tracker.isRawSealed(it) }

    fun kindClassIdOf(leaf: FirRegularClassSymbol): ClassId? =
        if (leaf.classKind == ClassKind.OBJECT) leaf.classId
        else leaf.companionObjectSymbol?.classId

    // label の既定 = 末端宣言の単純名（companion 自身が末端である場合はその宣言名がそのまま単純名になる）
    fun labelOf(leaf: FirRegularClassSymbol): String = leaf.classId.shortClassName.asString()

    // 同じ階層で同じ label を持つ他の末端（LABEL_CLASH の衝突相手）。基底の継承者一覧に自分が
    // まだ載っていない IC ラウンドでも、自分以外との衝突は同じ判定で得られる
    fun leavesSharingLabel(
        leaf: FirRegularClassSymbol,
        base: FirRegularClassSymbol,
    ): List<FirRegularClassSymbol> =
        labelIndexCache.getValue(base)[labelOf(leaf)].orEmpty().filterNot {
            it.classId == leaf.classId
        }

    fun starProjectedType(symbol: FirRegularClassSymbol): ConeClassLikeType =
        symbol.classId.constructClassLikeType(
            Array<ConeTypeProjection>(symbol.typeParameterSymbols.size) { ConeStarProjection }
        )

    // asEnumish の返り値型の規則（docs/コンパイラプラグイン設計01.md §5.4・docs/エッジケースへの対応方針.md §1.3）。
    // 規則 3（構成不能）の診断はチェッカーが担い、生成はフォールバック型のまま行う（docs/エッジケースへの対応方針.md §5）
    fun asEnumishReturnType(
        leaf: FirRegularClassSymbol,
        base: FirRegularClassSymbol,
    ): ConeClassLikeType {
        val enumishType = generatedEnumishClassId(base).constructClassLikeType()
        if (leaf.classKind == ClassKind.OBJECT) return leaf.defaultType()
        val companion = leaf.companionObjectSymbol ?: return enumishType
        if (isOurGenerated(companion)) return companion.defaultType()
        return if (effectiveVisibilityAtLeast(companion, leaf)) companion.defaultType()
        else enumishType
    }

    fun effectiveVisibilityAtLeast(
        target: FirClassLikeSymbol<*>,
        reference: FirClassLikeSymbol<*>,
    ): Boolean {
        val relation =
            target.resolvedStatus.effectiveVisibility.relation(
                reference.resolvedStatus.effectiveVisibility,
                session.typeContext,
            )
        return relation == EffectiveVisibility.Permissiveness.SAME ||
            relation == EffectiveVisibility.Permissiveness.MORE
    }

    // 生成 Enumish の継承者一覧: すべての kind + 階層内の手動実装（末端 class 自身による直接実装）。
    // 収集順のまま返す（setSealedClassInheritors のセッター側で FQN 順に正規化される。docs/コンパイラプラグイン設計01.md §5.2）。
    // 階層外の直接実装は ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY でエラーになるため、この 2 種で完全である
    fun computeGeneratedEnumishInheritors(base: FirRegularClassSymbol): List<ClassId> {
        val enumishClassId = generatedEnumishClassId(base)
        val result = mutableListOf<ClassId>()
        for (member in hierarchyMembersOf(base)) {
            if (!tracker.isRawSealed(member)) {
                kindClassIdOf(member)?.let(result::add)
            }
            if (
                member.classKind != ClassKind.OBJECT && directlyImplements(member, enumishClassId)
            ) {
                result.add(member.classId)
            }
        }
        return result.distinct()
    }

    fun directlyImplements(symbol: FirRegularClassSymbol, classId: ClassId): Boolean =
        symbol.resolvedSuperTypeRefs.any { it.coneType.classId == classId }

    // 解決済み supertype の全閉包（sealed に限らない全エッジ）。継承経路を見る診断（AMBIGUOUS_KIND・
    // MANUAL_SUPERTYPE_MISMATCH・MANUAL_MEMBER_CONFLICT・EXTENSION_SHADOWED）の判定に使う
    fun supertypeClosure(symbol: FirRegularClassSymbol): List<FirRegularClassSymbol> {
        val result = LinkedHashMap<ClassId, FirRegularClassSymbol>()
        collectClosure(symbol, result)
        return result.values.toList()
    }

    fun hasUserDeclaredNestedEnumish(base: FirRegularClassSymbol): Boolean =
        base.fir.declarations.any { declaration ->
            declaration is FirRegularClass &&
                declaration.name == EnumizeNames.ENUMISH_NAME &&
                !isOurGenerated(declaration.symbol)
        }

    fun declaredCallableNames(symbol: FirRegularClassSymbol): Set<Name> =
        symbol.fir.declarations.mapNotNullTo(LinkedHashSet()) { declaration ->
            when (declaration) {
                is FirNamedFunction -> declaration.name
                is FirProperty -> declaration.name
                else -> null
            }
        }

    // sealed 連鎖のみを上向きに辿り、到達できる相異なる @Enumize 基底を集める
    private fun computeBases(symbol: FirRegularClassSymbol): List<FirRegularClassSymbol> {
        val bases = LinkedHashMap<ClassId, FirRegularClassSymbol>()
        collectBases(symbol, LinkedHashSet(), bases)
        return bases.values.toList()
    }

    private fun collectBases(
        symbol: FirRegularClassSymbol,
        visited: MutableSet<ClassId>,
        result: LinkedHashMap<ClassId, FirRegularClassSymbol>,
    ) {
        if (!visited.add(symbol.classId)) return
        for (superSymbol in tracker.supertypeClassSymbols(symbol)) {
            if (!tracker.isRawSealed(superSymbol)) continue
            if (tracker.isEnumizeBase(superSymbol)) {
                result[superSymbol.classId] = superSymbol
            }
            collectBases(superSymbol, visited, result)
        }
    }

    private fun collectMembers(
        current: FirRegularClassSymbol,
        visitedSealed: MutableSet<ClassId>,
        result: LinkedHashMap<ClassId, FirRegularClassSymbol>,
    ) {
        if (!tracker.isRawSealed(current)) return
        if (!visitedSealed.add(current.classId)) return
        for (inheritorId in current.fir.getSealedClassInheritors(session)) {
            val inheritor = tracker.resolveClassSymbol(inheritorId) ?: continue
            if (
                result.putIfAbsent(inheritorId, inheritor) == null && tracker.isRawSealed(inheritor)
            ) {
                collectMembers(inheritor, visitedSealed, result)
            }
        }
    }

    private fun collectClosure(
        symbol: FirRegularClassSymbol,
        result: LinkedHashMap<ClassId, FirRegularClassSymbol>,
    ) {
        for (superSymbol in tracker.supertypeClassSymbols(symbol)) {
            if (result.putIfAbsent(superSymbol.classId, superSymbol) == null) {
                collectClosure(superSymbol, result)
            }
        }
    }
}

// セッション単一の階層照会コンポーネントへの入口（registrar が登録する）
val FirSession.enumizeHierarchyResolver: EnumizeHierarchyResolver
    get() =
        extensionService.extensionSessionComponents
            .filterIsInstance<EnumizeHierarchyResolver>()
            .single()
