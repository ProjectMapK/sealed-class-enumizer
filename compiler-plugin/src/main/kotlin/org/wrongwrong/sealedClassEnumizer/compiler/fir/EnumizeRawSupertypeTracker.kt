// ClassIdBasedLocality: 候補 ClassId はシンボル取得前に構成されるため、symbol provider へ照会しない
// 判定（local の除外）を ClassId 自身で行う必要がある
@file:OptIn(SymbolInternals::class, ClassIdBasedLocality::class)

package org.wrongwrong.sealedClassEnumizer.compiler.fir

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirImport
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
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
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames

// 設計01 §6.1: COMPANION_GENERATION フェーズで解決済み supertype に依存せずに「階層に属する候補か」を
// 判定するための raw supertype ref の追跡。フェーズ非依存の純関数として実装する
// （COMPILER_REQUIRED_ANNOTATIONS までに確定する情報だけを入力とし、同一入力に常に同一の答えを返す）。
// 解決済み ref（FirResolvedTypeRef）も受け付けるため、SUPER_TYPES 以降のフェーズからも同じ機構で辿れる。
//
// raw 段階の名前解決は、supertype のすべての表記（外側クラスのネスト・ファイルの明示 import・
// 同一パッケージのトップレベル・star import・FQN 表記と、typealias / import エイリアスの展開）を
// 扱い切る必要がある。階層判定は SUPER_TYPES 中の getCallableNamesForClass からも呼ばれ、その時点の
// 答えがコンパイラ側に固定されるため（設計01 §3）、raw で追えない表記は「後から解決済み型で正す」
// ことができず階層判定ごと欠落する。候補判定（companion 自動生成）も同じ追跡を使い、
// supertype の書き方で末端の扱いを変えない（設計01 §6.2）
class EnumizeRawSupertypeTracker(private val session: FirSession) {

    fun isHierarchyCandidate(classSymbol: FirRegularClassSymbol): Boolean =
        findEnumizeBase(classSymbol) != null

    fun findEnumizeBase(classSymbol: FirRegularClassSymbol): FirRegularClassSymbol? =
        findEnumizeBase(classSymbol, LinkedHashSet())

    // 解決済み supertype として与えられたクラス群を起点に基底を探す
    // （computeAdditionalSupertypes が受け取る resolvedSupertypes 用の入口）
    fun findEnumizeBaseAmong(superSymbols: List<FirRegularClassSymbol>): FirRegularClassSymbol? {
        for (superSymbol in superSymbols) {
            if (isEnumizeBase(superSymbol) && isRawSealed(superSymbol)) return superSymbol
            if (isRawSealed(superSymbol)) {
                val base = findEnumizeBase(superSymbol)
                if (base != null) return base
            }
        }
        return null
    }

    // supertype グラフ上で targetClassId へ到達できるか（Enumized の間接継承検出などの汎用到達判定）
    fun reachesSupertype(classSymbol: FirRegularClassSymbol, targetClassId: ClassId): Boolean =
        reachesSupertype(classSymbol, targetClassId, LinkedHashSet())

    // 述語はソース宣言にしかマッチしないため、IC ラウンド外のファイル由来（前ラウンドの
    // メタデータからの逆直列化）はアノテーションの直接照合で判定する（Retention が BINARY のため残る）
    fun isEnumizeBase(symbol: FirRegularClassSymbol): Boolean =
        session.predicateBasedProvider.matches(EnumizePredicates.ENUMIZE, symbol.fir) ||
            symbol.fir.hasAnnotation(EnumizeNames.ENUMIZE_ANNOTATION_CLASS_ID, session)

    fun isRawSealed(symbol: FirRegularClassSymbol): Boolean =
        symbol.rawStatus.modality == Modality.SEALED

    fun supertypeClassSymbols(classSymbol: FirRegularClassSymbol): List<FirRegularClassSymbol> =
        classSymbol.fir.superTypeRefs.mapNotNull { resolveSupertypeRef(it, classSymbol) }

    fun resolveClassSymbol(classId: ClassId?): FirRegularClassSymbol? {
        if (classId == null || classId.isLocal) return null
        return session.symbolProvider.getClassLikeSymbolByClassId(classId) as? FirRegularClassSymbol
    }

    // 解決済み型の照合は typealias を展開してから行う（型の同一性は表記に依らない）。
    // 型引数まで見る照合は展開後の型そのものを要するため、ClassId だけを取る入口と分ける
    fun expandedType(coneType: ConeKotlinType): ConeKotlinType = coneType.fullyExpandedType(session)

    // 解決済み型の展開はエイリアス自身の解決状況に依存し、エイリアスと階層が同一ファイルにある場合など
    // 展開が届かないことがある。頭の ClassId だけで足りる照合は、その場合に
    // raw 追跡（エイリアス自身のスコープでの名前解決）へ落として展開する
    fun expandedClassId(coneType: ConeKotlinType): ClassId? {
        val classId = expandedType(coneType).classId ?: return null
        val symbol =
            session.symbolProvider.getClassLikeSymbolByClassId(classId) as? FirTypeAliasSymbol
                ?: return classId
        return resolveAliasExpansion(symbol, LinkedHashSet())?.classId
    }

    fun resolveExpandedClassSymbol(coneType: ConeKotlinType): FirRegularClassSymbol? =
        resolveClassSymbol(expandedClassId(coneType))

    private fun findEnumizeBase(
        classSymbol: FirRegularClassSymbol,
        visited: MutableSet<ClassId>,
    ): FirRegularClassSymbol? {
        if (!visited.add(classSymbol.classId)) return null
        for (superSymbol in supertypeClassSymbols(classSymbol)) {
            if (isEnumizeBase(superSymbol) && isRawSealed(superSymbol)) return superSymbol
            if (isRawSealed(superSymbol)) {
                val base = findEnumizeBase(superSymbol, visited)
                if (base != null) return base
            }
        }
        return null
    }

    private fun reachesSupertype(
        classSymbol: FirRegularClassSymbol,
        targetClassId: ClassId,
        visited: MutableSet<ClassId>,
    ): Boolean {
        if (!visited.add(classSymbol.classId)) return false
        return supertypeClassSymbols(classSymbol).any { superSymbol ->
            superSymbol.classId == targetClassId ||
                reachesSupertype(superSymbol, targetClassId, visited)
        }
    }

    private fun resolveSupertypeRef(
        typeRef: FirTypeRef,
        useSite: FirClassLikeSymbol<*>,
    ): FirRegularClassSymbol? =
        when (typeRef) {
            is FirResolvedTypeRef -> resolveExpandedClassSymbol(typeRef.coneType)
            is FirUserTypeRef -> resolveUserTypeRef(typeRef, useSite)
            else -> null
        }

    private fun resolveUserTypeRef(
        typeRef: FirUserTypeRef,
        useSite: FirClassLikeSymbol<*>,
    ): FirRegularClassSymbol? {
        val classId = userTypeRefClassId(typeRef, useSite) ?: return null
        if (classId.isLocal) return null
        return when (val symbol = session.symbolProvider.getClassLikeSymbolByClassId(classId)) {
            is FirRegularClassSymbol -> symbol
            is FirTypeAliasSymbol -> resolveAliasExpansion(symbol, LinkedHashSet())
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

    // 先頭の名前をスコープ順に解決し、残りをネスト分類子として畳む。ファイルの import は 1 度だけ引いて
    // 下流（スコープ順の解決・FQN 表記としての解釈）へ渡す
    private fun userTypeRefClassId(
        typeRef: FirUserTypeRef,
        useSite: FirClassLikeSymbol<*>,
    ): ClassId? {
        val parts = typeRef.qualifier.map { it.name }
        val firstName = parts.firstOrNull() ?: return null
        val imports = importsOf(useSite)
        val scopeClassId = resolveFirstPartClassId(firstName, imports, useSite)
        // スコープ順で解決できない名前は FQN 表記として解釈する。ただし明示 import に束縛された名前は
        // その import 先が唯一の意味であり、FQN 表記ではない
        if (scopeClassId == null) {
            val bound = explicitBindingOf(imports, firstName) != null
            return if (bound) null else qualifiedNameClassId(parts, useSite)
        }
        return parts.drop(1).fold(scopeClassId) { id, part -> id.createNestedClassId(part) }
    }

    // 名前解決のスコープ順に忠実な候補選択: 外側クラスのネスト分類子（レキシカルに内側が優先）→
    // ファイルの明示 import → 同一パッケージのトップレベル → star import。最初に解決される候補のみを
    // 検査対象とし、外側候補への「ついで照会」はしない（名前を占有していれば typealias も候補確定）
    private fun resolveFirstPartClassId(
        firstName: Name,
        imports: List<FirImport>,
        useSite: FirClassLikeSymbol<*>,
    ): ClassId? {
        var outerId = useSite.classId.outerClassId
        while (outerId != null) {
            val nested = outerId.createNestedClassId(firstName)
            if (resolvesToClassLike(nested)) return nested
            outerId = outerId.outerClassId
        }
        // 明示 import は同一パッケージのトップレベルより優先されるため、束縛があればその import 先だけが
        // 候補になる（辿れない場合も後続の候補へは落とさない）
        val binding = explicitBindingOf(imports, firstName)
        if (binding != null) return importedClassId(binding, useSite)
        val topLevel = ClassId(useSite.classId.packageFqName, firstName)
        if (resolvesToClassLike(topLevel)) return topLevel
        return starImportedClassId(firstName, imports, useSite)
    }

    private fun importsOf(useSite: FirClassLikeSymbol<*>): List<FirImport> =
        session.firProvider.getFirClassifierContainerFileIfAny(useSite)?.imports.orEmpty()

    // その名前を束縛する明示 import（エイリアス名による束縛を含む）
    private fun explicitBindingOf(imports: List<FirImport>, name: Name): FirImport? =
        imports.firstOrNull { import ->
            !import.isAllUnder && (import.aliasName ?: import.importedFqName?.shortName()) == name
        }

    // import 先の ClassId（エイリアス名による束縛でも import 先そのものを指す）。別パッケージのクラスは
    // sealed の言語規則により階層の supertype になり得ないが、typealias は別パッケージにあっても
    // 展開先が階層の基底になりうるため、import 先はそのまま候補とする
    private fun importedClassId(import: FirImport, useSite: FirClassLikeSymbol<*>): ClassId? {
        val importedFqName = import.importedFqName ?: return null
        return resolvableClassId(importedFqName, useSite)
    }

    // star import（`import pkg.*`）経由の名前。同一パッケージのトップレベルの次に効くスコープ段であり、
    // 最初に解決される候補のみを採る
    private fun starImportedClassId(
        firstName: Name,
        imports: List<FirImport>,
        useSite: FirClassLikeSymbol<*>,
    ): ClassId? =
        imports
            .filter { it.isAllUnder }
            .firstNotNullOfOrNull { import ->
                import.importedFqName?.child(firstName)?.let { resolvableClassId(it, useSite) }
            }

    // パッケージ名から書かれた参照（FQN 表記）
    private fun qualifiedNameClassId(parts: List<Name>, useSite: FirClassLikeSymbol<*>): ClassId? {
        val fqName = parts.fold(FqName.ROOT) { name, part -> name.child(part) }
        return resolvableClassId(fqName, useSite)
    }

    // FqName の ClassId としての解釈。同一パッケージ配下のネスト参照 → トップレベル宣言 の順に実在する
    // 候補を採る（typealias は言語規則によりトップレベル限定のため、別パッケージのエイリアスもこの 2 形で届く）
    private fun resolvableClassId(fqName: FqName, useSite: FirClassLikeSymbol<*>): ClassId? {
        if (fqName.isRoot) return null
        val nested = classIdInPackage(useSite.classId.packageFqName, fqName)
        if (nested != null && resolvesToClassLike(nested)) return nested
        return ClassId(fqName.parent(), fqName.shortName()).takeIf(::resolvesToClassLike)
    }

    // packageFqName 配下を指す fqName の ClassId（ネストクラスへの参照も畳んで構成する）
    private fun classIdInPackage(packageFqName: FqName, fqName: FqName): ClassId? {
        val packageSegments = packageFqName.pathSegments()
        val segments = fqName.pathSegments()
        if (segments.size <= packageSegments.size) return null
        if (segments.subList(0, packageSegments.size) != packageSegments) return null
        val relativeSegments = segments.subList(packageSegments.size, segments.size)
        return relativeSegments.drop(1).fold(ClassId(packageFqName, relativeSegments.first())) {
            id,
            name ->
            id.createNestedClassId(name)
        }
    }

    private fun resolvesToClassLike(classId: ClassId): Boolean =
        !classId.isLocal && session.symbolProvider.getClassLikeSymbolByClassId(classId) != null
}
