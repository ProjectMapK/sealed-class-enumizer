@file:OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)

package org.wrongwrong.sealedClassEnumizer.compiler.fir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.getSealedClassInheritors
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
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

// 解決済み情報（sealed inheritors 属性・解決済み supertype・実効可視性）を用いる階層照会。
// SUPER_TYPES 以降のフェーズ（メンバー生成・チェッカー・lazy inheritors 計算）から使う。
// 用語（階層・末端・中間 sealed・kind）は設計00 §1 に従う。
class EnumizeHierarchyResolver(val session: FirSession) {
    val tracker: EnumizeRawSupertypeTracker = EnumizeRawSupertypeTracker(session)

    fun isEnumizeBase(symbol: FirRegularClassSymbol): Boolean =
        tracker.isEnumizeBase(symbol) && tracker.isRawSealed(symbol)

    fun isSealed(symbol: FirRegularClassSymbol): Boolean = tracker.isRawSealed(symbol)

    fun isOurGenerated(symbol: FirBasedSymbol<*>): Boolean =
        (symbol.origin as? FirDeclarationOrigin.Plugin)?.key == EnumizeKey

    fun isOurGeneratedDeclaration(declaration: FirDeclaration): Boolean =
        (declaration.origin as? FirDeclarationOrigin.Plugin)?.key == EnumizeKey

    fun generatedEnumishClassId(base: FirRegularClassSymbol): ClassId =
        base.classId.createNestedClassId(EnumizeNames.ENUMISH_NAME)

    fun generatedEnumishCompanionClassId(base: FirRegularClassSymbol): ClassId =
        generatedEnumishClassId(base).createNestedClassId(SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT)

    // sealed 連鎖のみを上向きに辿り、到達できる相異なる @Enumize 基底をすべて返す
    // （2 つ以上 = ENUMIZE_MULTIPLE_FAMILIES。設計01 §7.2）
    fun findBases(symbol: FirRegularClassSymbol): List<FirRegularClassSymbol> {
        val result = LinkedHashMap<ClassId, FirRegularClassSymbol>()
        collectBases(symbol, LinkedHashSet(), result)
        return result.values.toList()
    }

    fun findSingleBase(symbol: FirRegularClassSymbol): FirRegularClassSymbol? =
        findBases(symbol).firstOrNull()

    fun isLeaf(symbol: FirRegularClassSymbol): Boolean =
        !tracker.isRawSealed(symbol) && findBases(symbol).isNotEmpty()

    // 基底の sealed inheritors 属性を再帰展開した階層の全メンバー（中間 sealed を含む・基底自身を除く）。
    // 並べ替えは行わず、コンパイラが提供する継承者リストの走査順のまま返す（設計00 §6.2）
    fun hierarchyMembersOf(base: FirRegularClassSymbol): List<FirRegularClassSymbol> {
        val result = LinkedHashMap<ClassId, FirRegularClassSymbol>()
        collectMembers(base, LinkedHashSet(), result)
        return result.values.toList()
    }

    // 階層の末端のみ（中間 sealed の位置にその継承者が入れ子展開された順序）
    fun leavesOf(base: FirRegularClassSymbol): List<FirRegularClassSymbol> =
        hierarchyMembersOf(base).filterNot { tracker.isRawSealed(it) }

    fun kindClassIdOf(leaf: FirRegularClassSymbol): ClassId? =
        if (leaf.classKind == ClassKind.OBJECT) leaf.classId else leaf.companionObjectSymbol?.classId

    // label の既定 = 末端宣言の単純名（companion 自身が末端である場合はその宣言名がそのまま単純名になる）
    fun labelOf(leaf: FirRegularClassSymbol): String = leaf.classId.shortClassName.asString()

    fun starProjectedType(symbol: FirRegularClassSymbol): ConeClassLikeType =
        symbol.classId.constructClassLikeType(
            Array<ConeTypeProjection>(symbol.typeParameterSymbols.size) { ConeStarProjection }
        )

    // asEnumish の返り値型の規則（設計01 §5.4・エッジケースへの対応方針 §1.3）。
    // 規則 3（構成不能）の診断はチェッカーが担い、生成はフォールバック型のまま行う（エッジ §5）
    fun asEnumishReturnType(leaf: FirRegularClassSymbol, base: FirRegularClassSymbol): ConeClassLikeType {
        val enumishType = generatedEnumishClassId(base).constructClassLikeType()
        if (leaf.classKind == ClassKind.OBJECT) return leaf.defaultType()
        val companion = leaf.companionObjectSymbol ?: return enumishType
        if (isOurGenerated(companion)) return companion.defaultType()
        return if (effectiveVisibilityAtLeast(companion, leaf)) companion.defaultType() else enumishType
    }

    fun effectiveVisibilityAtLeast(target: FirClassLikeSymbol<*>, reference: FirClassLikeSymbol<*>): Boolean {
        val relation = target.resolvedStatus.effectiveVisibility
            .relation(reference.resolvedStatus.effectiveVisibility, session.typeContext)
        return relation == EffectiveVisibility.Permissiveness.SAME ||
            relation == EffectiveVisibility.Permissiveness.MORE
    }

    // 生成 Enumish の継承者一覧: すべての kind + 手動実装（生成 Enumish の直接実装）。
    // 収集順のまま返す（setSealedClassInheritors のセッター側で FQN 順に正規化される。設計01 §5.2）。
    // 手動実装のうち階層外のもの（V1-(e)）は、sealed の継承者が同一パッケージに閉じる言語規則
    // （コンパイラ本体の収集器も同一パッケージでガードする）を利用し、基底のパッケージの
    // ソースファイルに閉じた探索で列挙する。コンパイラ本体の収集器はソース宣言の sealed しか
    // 属性設定の対象にしないため（実測）、生成 sealed の分は自前で列挙するしかない
    fun computeGeneratedEnumishInheritors(base: FirRegularClassSymbol): List<ClassId> {
        val enumishClassId = generatedEnumishClassId(base)
        val result = mutableListOf<ClassId>()
        for (member in hierarchyMembersOf(base)) {
            if (!tracker.isRawSealed(member)) {
                kindClassIdOf(member)?.let(result::add)
            }
            if (member.classKind != ClassKind.OBJECT && directlyImplements(member, enumishClassId)) {
                result.add(member.classId)
            }
        }
        result += packageLocalDirectImplementors(base, enumishClassId)
        return result.distinct()
    }

    fun directlyImplements(symbol: FirRegularClassSymbol, classId: ClassId): Boolean =
        symbol.resolvedSuperTypeRefs.any { it.coneType.classId == classId }

    // 基底のパッケージのソースファイルを走査し、生成 Enumish を直接実装するクラスを列挙する。
    // 網羅性検査以降にしか走らない lazy 計算からのみ呼ぶこと（全宣言の supertype が解決済みである前提）
    private fun packageLocalDirectImplementors(
        base: FirRegularClassSymbol,
        enumishClassId: ClassId,
    ): List<ClassId> {
        val result = mutableListOf<ClassId>()
        for (file in session.firProvider.getFirFilesByPackage(base.classId.packageFqName)) {
            collectDirectImplementors(file.declarations, enumishClassId, result)
        }
        return result
    }

    private fun collectDirectImplementors(
        declarations: List<FirDeclaration>,
        enumishClassId: ClassId,
        result: MutableList<ClassId>,
    ) {
        for (declaration in declarations) {
            if (declaration !is FirRegularClass) continue
            if (directlyImplements(declaration.symbol, enumishClassId)) {
                result.add(declaration.symbol.classId)
            }
            collectDirectImplementors(declaration.declarations, enumishClassId, result)
        }
    }

    // 解決済み supertype の全閉包（sealed に限らない全エッジ）。ENUMIZE_AMBIGUOUS_KIND の判定に使う
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

    private fun collectBases(
        symbol: FirRegularClassSymbol,
        visited: MutableSet<ClassId>,
        result: LinkedHashMap<ClassId, FirRegularClassSymbol>,
    ) {
        if (!visited.add(symbol.classId)) return
        for (superSymbol in tracker.supertypeClassSymbols(symbol, followTypeAliases = true)) {
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
            if (result.putIfAbsent(inheritorId, inheritor) == null && tracker.isRawSealed(inheritor)) {
                collectMembers(inheritor, visitedSealed, result)
            }
        }
    }

    private fun collectClosure(
        symbol: FirRegularClassSymbol,
        result: LinkedHashMap<ClassId, FirRegularClassSymbol>,
    ) {
        for (superSymbol in tracker.supertypeClassSymbols(symbol, followTypeAliases = true)) {
            if (result.putIfAbsent(superSymbol.classId, superSymbol) == null) {
                collectClosure(superSymbol, result)
            }
        }
    }
}
