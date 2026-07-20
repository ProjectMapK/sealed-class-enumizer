// ClassIdBasedLocality: 候補 ClassId はシンボル取得前に構成されるため、symbol provider へ照会しない
// 判定（local の除外）を ClassId 自身で行う必要がある
@file:OptIn(SymbolInternals::class, ClassIdBasedLocality::class)

package org.wrongwrong.sealedClassEnumizer.compiler.fir

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.FirUserTypeRef
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.ClassIdBasedLocality
import org.jetbrains.kotlin.name.Name

// 設計01 §6.1: COMPANION_GENERATION フェーズで解決済み supertype に依存せずに「階層に属する候補か」を
// 判定するための raw supertype ref の保守的追跡。フェーズ非依存の純関数として実装する
// （COMPILER_REQUIRED_ANNOTATIONS までに確定する情報だけを入力とし、同一入力に常に同一の答えを返す）。
// 解決済み ref（FirResolvedTypeRef）も受け付けるため、SUPER_TYPES 以降のフェーズからも同じ機構で辿れる。
//
// followTypeAliases の使い分け:
// - true  … supertype 注入・階層メンバー判定・チェッカー（設計01 §6.2 の「明示的な companion は常に
//           完全なワークアラウンド」を成立させるため、typealias の raw な expandedTypeRef も辿る）
// - false … companion 自動生成の候補判定（typealias 経由は意図的な見逃しとし、
//           ENUMIZE_COMPANION_REQUIRED で明示的な companion を促す契約を維持する）
class EnumizeRawSupertypeTracker(private val session: FirSession) {

    fun isHierarchyCandidate(classSymbol: FirRegularClassSymbol, followTypeAliases: Boolean): Boolean =
        findEnumizeBase(classSymbol, followTypeAliases) != null

    fun findEnumizeBase(classSymbol: FirRegularClassSymbol, followTypeAliases: Boolean): FirRegularClassSymbol? =
        findEnumizeBase(classSymbol, followTypeAliases, LinkedHashSet())

    fun isEnumizeBase(symbol: FirRegularClassSymbol): Boolean =
        session.predicateBasedProvider.matches(EnumizePredicates.ENUMIZE, symbol.fir)

    fun isRawSealed(symbol: FirRegularClassSymbol): Boolean =
        symbol.rawStatus.modality == Modality.SEALED

    fun supertypeClassSymbols(
        classSymbol: FirRegularClassSymbol,
        followTypeAliases: Boolean,
    ): List<FirRegularClassSymbol> =
        classSymbol.fir.superTypeRefs.mapNotNull { resolveSupertypeRef(it, classSymbol, followTypeAliases) }

    fun resolveClassSymbol(classId: ClassId?): FirRegularClassSymbol? {
        if (classId == null || classId.isLocal) return null
        return session.symbolProvider.getClassLikeSymbolByClassId(classId) as? FirRegularClassSymbol
    }

    // 解決済み型は typealias を展開してから ClassId を取る
    fun resolveExpandedClassSymbol(coneType: ConeKotlinType): FirRegularClassSymbol? =
        resolveClassSymbol(coneType.fullyExpandedType(session).classId)

    private fun findEnumizeBase(
        classSymbol: FirRegularClassSymbol,
        followTypeAliases: Boolean,
        visited: MutableSet<ClassId>,
    ): FirRegularClassSymbol? {
        if (!visited.add(classSymbol.classId)) return null
        for (superSymbol in supertypeClassSymbols(classSymbol, followTypeAliases)) {
            if (isEnumizeBase(superSymbol) && isRawSealed(superSymbol)) return superSymbol
            if (isRawSealed(superSymbol)) {
                val base = findEnumizeBase(superSymbol, followTypeAliases, visited)
                if (base != null) return base
            }
        }
        return null
    }

    private fun resolveSupertypeRef(
        typeRef: FirTypeRef,
        useSite: FirClassLikeSymbol<*>,
        followTypeAliases: Boolean,
    ): FirRegularClassSymbol? =
        when (typeRef) {
            is FirResolvedTypeRef -> resolveExpandedClassSymbol(typeRef.coneType)
            is FirUserTypeRef -> resolveUserTypeRef(typeRef, useSite, followTypeAliases)
            else -> null
        }

    private fun resolveUserTypeRef(
        typeRef: FirUserTypeRef,
        useSite: FirClassLikeSymbol<*>,
        followTypeAliases: Boolean,
    ): FirRegularClassSymbol? {
        val classId = userTypeRefClassId(typeRef, useSite) ?: return null
        if (classId.isLocal) return null
        return when (val symbol = session.symbolProvider.getClassLikeSymbolByClassId(classId)) {
            is FirRegularClassSymbol -> symbol
            is FirTypeAliasSymbol ->
                if (followTypeAliases) resolveAliasExpansion(symbol, LinkedHashSet()) else null
            else -> null
        }
    }

    // typealias の展開先を raw のまま辿る。expandedTypeRef が未解決（FirUserTypeRef）の場合は、
    // typealias がトップレベル限定である言語規則により、エイリアス自身のファイル・パッケージを
    // 文脈として同じスコープ規則で解決する
    private fun resolveAliasExpansion(
        alias: FirTypeAliasSymbol,
        visitedAliases: MutableSet<ClassId>,
    ): FirRegularClassSymbol? {
        if (!visitedAliases.add(alias.classId)) return null
        return when (val expanded = alias.fir.expandedTypeRef) {
            is FirResolvedTypeRef -> resolveExpandedClassSymbol(expanded.coneType)
            is FirUserTypeRef -> {
                val classId = userTypeRefClassId(expanded, alias) ?: return null
                if (classId.isLocal) return null
                when (val symbol = session.symbolProvider.getClassLikeSymbolByClassId(classId)) {
                    is FirRegularClassSymbol -> symbol
                    is FirTypeAliasSymbol -> resolveAliasExpansion(symbol, visitedAliases)
                    else -> null
                }
            }
            else -> null
        }
    }

    private fun userTypeRefClassId(typeRef: FirUserTypeRef, useSite: FirClassLikeSymbol<*>): ClassId? {
        val parts = typeRef.qualifier.map { it.name }
        val firstName = parts.firstOrNull() ?: return null
        val scopeClassId = resolveFirstPartClassId(firstName, useSite) ?: return null
        return parts.drop(1).fold(scopeClassId) { id, part -> id.createNestedClassId(part) }
    }

    // 名前解決のスコープ順に忠実な候補選択: 外側クラスのネスト分類子（レキシカルに内側が優先）→
    // 同一パッケージのトップレベル。最初に解決される候補のみを検査対象とし、外側候補への
    // 「ついで照会」はしない。名前を占有していれば typealias も候補確定として扱う（辿るかどうかは
    // followTypeAliases に従う）。import エイリアス・FQN 表記はここでは追えず、
    // 見逃しは ENUMIZE_COMPANION_REQUIRED で顕在化する（設計01 §6.2）
    private fun resolveFirstPartClassId(firstName: Name, useSite: FirClassLikeSymbol<*>): ClassId? {
        var outerId = useSite.classId.outerClassId
        while (outerId != null) {
            val nested = outerId.createNestedClassId(firstName)
            if (resolvesToClassLike(nested)) return nested
            outerId = outerId.outerClassId
        }
        if (hasForeignExplicitImport(firstName, useSite)) return null
        val topLevel = ClassId(useSite.classId.packageFqName, firstName)
        return topLevel.takeIf(::resolvesToClassLike)
    }

    private fun resolvesToClassLike(classId: ClassId): Boolean =
        !classId.isLocal && session.symbolProvider.getClassLikeSymbolByClassId(classId) != null

    // 明示 import は同一パッケージのトップレベルより優先されるため、同名で別パッケージからの import が
    // ある場合はその名前を候補から外す（別パッケージの型は sealed の言語規則により階層の supertype になり得ない）
    private fun hasForeignExplicitImport(name: Name, useSite: FirClassLikeSymbol<*>): Boolean {
        val file = session.firProvider.getFirClassifierContainerFileIfAny(useSite) ?: return false
        val samePackageFqName = useSite.classId.packageFqName.child(name)
        return file.imports.any { import ->
            !import.isAllUnder &&
                (import.aliasName ?: import.importedFqName?.shortName()) == name &&
                import.importedFqName != samePackageFqName
        }
    }
}
