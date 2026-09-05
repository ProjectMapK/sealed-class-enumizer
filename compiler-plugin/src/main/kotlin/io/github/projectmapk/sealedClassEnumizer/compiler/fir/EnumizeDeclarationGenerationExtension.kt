@file:OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)

package io.github.projectmapk.sealedClassEnumizer.compiler.fir

import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeKey
import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeNames
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.setSealedClassInheritors
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createCompanionObject
import org.jetbrains.kotlin.fir.plugin.createDefaultPrivateConstructor
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.plugin.createNestedClass
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinTypeProjectionOut
import org.jetbrains.kotlin.fir.types.ConeTypeProjection
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.name.StandardClassIds

// 宣言の形状の生成（docs/コンパイラプラグイン設計01.md §5・§6）。ボディは一切作らず、シグネチャは継承者の集合に依存させない（P2）。
// 唯一の例外は生成 Enumish への sealed inheritors 属性の lazy 登録である（§5.2）。
class EnumizeDeclarationGenerationExtension(session: FirSession) :
    FirDeclarationGenerationExtension(session) {
    // コンポーネント群の生成順に依存しないよう、初回コールバック時に解決する
    private val resolver: EnumizeHierarchyResolver by lazy { session.enumizeHierarchyResolver }
    private val tracker: EnumizeRawSupertypeTracker
        get() = resolver.tracker

    // 生成 Enumish → その生成 Companion。FirCompanionGenerationTransformer はソース宣言しか走査せず、
    // 生成クラスには companionObjectSymbol を連結しないため、生成時に自前で構築・連結して同一インスタンスを返す
    private val enumishCompanions = HashMap<FirRegularClassSymbol, FirRegularClassSymbol>()

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(EnumizePredicates.ENUMIZE)
    }

    // ---- ネスト分類子（名前の確定は COMPANION_GENERATION フェーズ） ----

    override fun getNestedClassifiersNames(
        classSymbol: FirClassSymbol<*>,
        context: NestedClassGenerationContext,
    ): Set<Name> {
        val symbol = classSymbol as? FirRegularClassSymbol ?: return emptySet()
        if (symbol.isLocal) return emptySet()
        return when {
            isGeneratedEnumish(symbol) -> setOf(SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT)
            session.predicateBasedProvider.matches(EnumizePredicates.ENUMIZE, symbol.fir) ->
                nestedNamesForBase(symbol)
            isCompanionGenerationCandidate(symbol) ->
                setOf(SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT)
            else -> emptySet()
        }
    }

    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext,
    ): FirClassLikeSymbol<*>? {
        val ownerSymbol = owner as? FirRegularClassSymbol ?: return null
        return when {
            name == EnumizeNames.ENUMISH_NAME &&
                session.predicateBasedProvider.matches(
                    EnumizePredicates.ENUMIZE,
                    ownerSymbol.fir,
                ) -> generateEnumishClass(ownerSymbol)
            name != SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT -> null
            isGeneratedEnumish(ownerSymbol) -> enumishCompanionOf(ownerSymbol)
            isCompanionGenerationCandidate(ownerSymbol) -> generateLeafCompanion(ownerSymbol)
            else -> null
        }
    }

    // ---- メンバー生成（名前集合の確定は SUPER_TYPES・シグネチャ構築は STATUS 以降。docs/コンパイラプラグイン設計01.md §3） ----

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> {
        val symbol = classSymbol as? FirRegularClassSymbol ?: return emptySet()
        if (symbol.isLocal) return emptySet()
        val names = mutableSetOf<Name>()
        // 生成 companion は候補判定の誤検知（外側が末端でなかった）でもコンストラクタだけは必要（docs/コンパイラプラグイン設計01.md §6.2）
        if (symbol.rawStatus.isCompanion && resolver.isGeneratedByEnumize(symbol)) {
            names += SpecialNames.INIT
        }
        val role = roleOf(symbol)
        when (role) {
            is EnumizeGenerationRole.GeneratedEnumish -> {
                names += EnumizeNames.ENUMISH_COMPANION_PROPERTY
                names += EnumizeNames.ENUMIZED_CLASS_PROPERTY
            }
            is EnumizeGenerationRole.GeneratedEnumishCompanion -> {
                names += EnumizeNames.ENTRIES
                names += EnumizeNames.VALUE_OF
                names += EnumizeNames.VALUE_OF_OR_NULL
            }
            is EnumizeGenerationRole.KindCompanion ->
                names +=
                    notManuallyDeclared(
                        symbol,
                        EnumizeNames.LABEL,
                        EnumizeNames.ENUMIZED_CLASS_PROPERTY,
                    )
            is EnumizeGenerationRole.LeafObject ->
                names +=
                    notManuallyDeclared(
                        symbol,
                        EnumizeNames.LABEL,
                        EnumizeNames.ENUMIZED_CLASS_PROPERTY,
                        EnumizeNames.AS_ENUMISH,
                    )
            is EnumizeGenerationRole.LeafClass ->
                names += notManuallyDeclared(symbol, EnumizeNames.AS_ENUMISH)
            null -> Unit
        }
        return names
    }

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirPropertySymbol> {
        val owner = context?.owner as? FirRegularClassSymbol ?: return emptyList()
        val role = roleOf(owner) ?: return emptyList()
        val property =
            when (callableId.callableName) {
                EnumizeNames.ENUMISH_COMPANION_PROPERTY ->
                    (role as? EnumizeGenerationRole.GeneratedEnumish)?.let {
                        enumishCompanionProperty(owner)
                    }
                EnumizeNames.ENTRIES ->
                    (role as? EnumizeGenerationRole.GeneratedEnumishCompanion)?.let {
                        entriesProperty(owner)
                    }
                EnumizeNames.LABEL ->
                    when (role) {
                        is EnumizeGenerationRole.KindCompanion -> labelProperty(owner)
                        is EnumizeGenerationRole.LeafObject -> labelProperty(owner)
                        else -> null
                    }
                EnumizeNames.ENUMIZED_CLASS_PROPERTY ->
                    when (role) {
                        is EnumizeGenerationRole.GeneratedEnumish ->
                            enumishEnumizedClassProperty(owner, role.base)
                        is EnumizeGenerationRole.KindCompanion ->
                            kindEnumizedClassProperty(owner, role.leaf)
                        is EnumizeGenerationRole.LeafObject ->
                            kindEnumizedClassProperty(owner, owner)
                        else -> null
                    }
                else -> null
            } ?: return emptyList()
        return listOf(property.symbol)
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?,
    ): List<FirNamedFunctionSymbol> {
        val owner = context?.owner as? FirRegularClassSymbol ?: return emptyList()
        val role = roleOf(owner) ?: return emptyList()
        val function =
            when (callableId.callableName) {
                EnumizeNames.VALUE_OF ->
                    (role as? EnumizeGenerationRole.GeneratedEnumishCompanion)?.let {
                        valueOfFunction(owner, isOrNull = false)
                    }
                EnumizeNames.VALUE_OF_OR_NULL ->
                    (role as? EnumizeGenerationRole.GeneratedEnumishCompanion)?.let {
                        valueOfFunction(owner, isOrNull = true)
                    }
                EnumizeNames.AS_ENUMISH ->
                    when (role) {
                        is EnumizeGenerationRole.LeafObject -> leafObjectAsEnumishFunction(owner)
                        is EnumizeGenerationRole.LeafClass ->
                            leafClassAsEnumishFunction(owner, role.base)
                        else -> null
                    }
                else -> null
            } ?: return emptyList()
        return listOf(function.symbol)
    }

    override fun generateConstructors(
        context: MemberGenerationContext
    ): List<FirConstructorSymbol> {
        val owner = context.owner as? FirRegularClassSymbol ?: return emptyList()
        if (!owner.rawStatus.isCompanion || !resolver.isGeneratedByEnumize(owner))
            return emptyList()
        return listOf(createDefaultPrivateConstructor(owner, EnumizeKey).symbol)
    }

    // ---- 役割判定・候補判定 ----

    // COMPANION_GENERATION の候補判定（docs/コンパイラプラグイン設計01.md §6.1）。COMPILER_REQUIRED_ANNOTATIONS までに確定する
    // 情報（述語・classKind・rawStatus・raw superTypeRefs・import・symbol provider）だけを使う純関数
    private fun isCompanionGenerationCandidate(symbol: FirRegularClassSymbol): Boolean {
        val supportedKind =
            symbol.classKind == ClassKind.CLASS ||
                symbol.classKind == ClassKind.INTERFACE ||
                symbol.classKind == ClassKind.ENUM_CLASS
        if (!supportedKind) return false
        val status = symbol.rawStatus
        if (status.isCompanion || status.isInner || status.modality == Modality.SEALED) return false
        if (hasForeignCompanion(symbol)) return false
        // 候補判定は所属判定と同じ材料で行う（docs/コンパイラプラグイン設計01.md §6.2）。supertype の書き方（直接名・FQN 表記・
        // 明示 import・star import・typealias / import エイリアス）で末端の扱いを変えない
        return tracker.isHierarchyCandidate(symbol)
    }

    // 自前の生成結果（連結済みの生成 companion）を候補判定の入力にしない。
    // getNestedClassifiersNames は COMPANION_GENERATION 後にも再評価される（KMP では
    // 宣言側とは別のセッションが直列化時にネスト分類子スコープを構築し直す）ため、
    // 生成済みを理由に false へ転ずるとネスト索引から companion が落ちる（docs/コンパイラプラグイン設計01.md §6.1 の
    // 「同一入力に常に同一の答えを返す」要件）。手動宣言の companion は従来どおり候補から外す
    private fun hasForeignCompanion(symbol: FirRegularClassSymbol): Boolean {
        val companion = symbol.companionObjectSymbol ?: return false
        return !resolver.isGeneratedByEnumize(companion)
    }

    // 所属（membership）は resolver が一度だけ計算した事実を読み、役割へ写像する。
    // 非所属・複数階層に属するエラー構成では membership が null になり、生成しない（診断はチェッカーが担う）
    private fun roleOf(symbol: FirRegularClassSymbol): EnumizeGenerationRole? {
        if (isGeneratedEnumish(symbol)) {
            val base = tracker.resolveClassSymbol(symbol.classId.outerClassId) ?: return null
            return EnumizeGenerationRole.GeneratedEnumish(base)
        }
        if (symbol.rawStatus.isCompanion) {
            // companion 自身が末端である場合（階層外クラスの companion が単独で末端になる許容構成）は
            // 末端 object として扱う。kind = その companion・label = companion の宣言名
            // （docs/コンパイラプラグイン設計01.md §7.2）
            val selfMembership = resolver.membershipOf(symbol)
            if (selfMembership != null && selfMembership.isLeaf) {
                return EnumizeGenerationRole.LeafObject(selfMembership.base)
            }
            val outer = tracker.resolveClassSymbol(symbol.classId.outerClassId) ?: return null
            if (isGeneratedEnumish(outer))
                return EnumizeGenerationRole.GeneratedEnumishCompanion(outer)
            val outerMembership = resolver.membershipOf(outer) ?: return null
            if (!outerMembership.isLeaf) return null
            return EnumizeGenerationRole.KindCompanion(outer, outerMembership.base)
        }
        val membership = resolver.membershipOf(symbol) ?: return null
        if (!membership.isLeaf) return null
        return if (symbol.classKind == ClassKind.OBJECT) {
            EnumizeGenerationRole.LeafObject(membership.base)
        } else {
            EnumizeGenerationRole.LeafClass(membership.base)
        }
    }

    private fun isGeneratedEnumish(symbol: FirRegularClassSymbol): Boolean =
        resolver.isGeneratedByEnumize(symbol) &&
            symbol.classId.shortClassName == EnumizeNames.ENUMISH_NAME

    private fun nestedNamesForBase(base: FirRegularClassSymbol): Set<Name> =
        if (resolver.hasUserDeclaredNestedEnumish(base)) emptySet()
        else setOf(EnumizeNames.ENUMISH_NAME)

    // ユーザーが同名メンバーを手動宣言している場合は生成しない（ENUMIZE_MEMBER_CONFLICT はチェッカーが報告）
    private fun notManuallyDeclared(symbol: FirRegularClassSymbol, vararg names: Name): Set<Name> {
        val declared = resolver.declaredCallableNames(symbol)
        return names.filterNotTo(mutableSetOf()) { it in declared }
    }

    // ---- 生成本体 ----

    private fun generateEnumishClass(base: FirRegularClassSymbol): FirClassLikeSymbol<*>? {
        if (resolver.hasUserDeclaredNestedEnumish(base)) return null
        val enumish =
            createNestedClass(
                base,
                EnumizeNames.ENUMISH_NAME,
                EnumizeKey,
                classKind = ClassKind.INTERFACE,
            ) {
                modality = Modality.SEALED
                superType(EnumizeNames.ENUMISH_CLASS_ID.constructClassLikeType())
            }
        enumish.replaceCompanionObjectSymbol(enumishCompanionOf(enumish.symbol))
        // 継承者一覧の lazy 登録（V1）。計算は登録せず遅延し、実際の列挙は網羅性検査以降に走る（docs/コンパイラプラグイン設計01.md §5.2）
        enumish.setSealedClassInheritors { resolver.computeGeneratedEnumishInheritors(base) }
        return enumish.symbol
    }

    // 生成 Enumish の companion は、ネスト分類子としての要求（generateNestedClassLikeDeclaration）と
    // Enumish 自身への連結の両方から引かれるため、階層ごとに 1 インスタンスを作って共有する
    private fun enumishCompanionOf(enumish: FirRegularClassSymbol): FirRegularClassSymbol =
        enumishCompanions.getOrPut(enumish) {
            val companion =
                createCompanionObject(enumish, EnumizeKey) {
                    superType(
                        EnumizeNames.ENUMISH_COMPANION_CLASS_ID.constructClassLikeType(
                            arrayOf<ConeTypeProjection>(enumish.classId.constructClassLikeType())
                        )
                    )
                }
            EnumizeOwnerGeneratorPatch.stamp(companion, this)
            companion.symbol
        }

    // supertype transformer はプラグイン生成の companion を訪問しないため、生成 Enumish と同様に
    // supertype を生成時へ直接指定する（docs/コンパイラプラグイン設計01.md §5.1）。候補判定の誤検知時は解決済み supertype による
    // 正式判定が効かないままだが、その構成はチェッカーの後続診断で顕在化する
    private fun generateLeafCompanion(leaf: FirRegularClassSymbol): FirClassLikeSymbol<*> {
        val base = tracker.findEnumizeBase(leaf)
        val companion =
            createCompanionObject(leaf, EnumizeKey) {
                if (base != null && !resolver.hasUserDeclaredNestedEnumish(base)) {
                    superType(resolver.generatedEnumishClassId(base).constructClassLikeType())
                }
            }
        return companion.symbol
    }

    private fun enumishCompanionProperty(enumish: FirRegularClassSymbol) =
        createMemberProperty(
            enumish,
            EnumizeKey,
            EnumizeNames.ENUMISH_COMPANION_PROPERTY,
            enumish.classId
                .createNestedClassId(SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT)
                .constructClassLikeType(),
            hasBackingField = false,
        ) {
            modality = Modality.OPEN
            status { isOverride = true }
        }

    private fun enumishEnumizedClassProperty(
        enumish: FirRegularClassSymbol,
        base: FirRegularClassSymbol,
    ) =
        createMemberProperty(
            enumish,
            EnumizeKey,
            EnumizeNames.ENUMIZED_CLASS_PROPERTY,
            StandardClassIds.KClass.constructClassLikeType(
                arrayOf(ConeKotlinTypeProjectionOut(resolver.starProjectedType(base)))
            ),
            hasBackingField = false,
        ) {
            modality = Modality.ABSTRACT
            status { isOverride = true }
        }

    private fun entriesProperty(companion: FirRegularClassSymbol): FirProperty {
        val enumishType = companion.classId.outerClassId!!.constructClassLikeType()
        return createMemberProperty(
            companion,
            EnumizeKey,
            EnumizeNames.ENTRIES,
            StandardClassIds.List.constructClassLikeType(arrayOf<ConeTypeProjection>(enumishType)),
            hasBackingField = false,
        ) {
            status { isOverride = true }
        }
    }

    private fun labelProperty(kind: FirRegularClassSymbol) =
        createMemberProperty(
            kind,
            EnumizeKey,
            EnumizeNames.LABEL,
            session.builtinTypes.stringType.coneType,
            hasBackingField = false,
        ) {
            status { isOverride = true }
        }

    private fun kindEnumizedClassProperty(
        kind: FirRegularClassSymbol,
        leaf: FirRegularClassSymbol,
    ) =
        createMemberProperty(
            kind,
            EnumizeKey,
            EnumizeNames.ENUMIZED_CLASS_PROPERTY,
            StandardClassIds.KClass.constructClassLikeType(
                arrayOf<ConeTypeProjection>(resolver.starProjectedType(leaf))
            ),
            hasBackingField = false,
        ) {
            status { isOverride = true }
        }

    private fun valueOfFunction(companion: FirRegularClassSymbol, isOrNull: Boolean) =
        createMemberFunction(
            companion,
            EnumizeKey,
            if (isOrNull) EnumizeNames.VALUE_OF_OR_NULL else EnumizeNames.VALUE_OF,
            companion.classId.outerClassId!!.constructClassLikeType(isMarkedNullable = isOrNull),
        ) {
            valueParameter(EnumizeNames.VALUE_PARAMETER, session.builtinTypes.stringType.coneType)
            status { isOverride = true }
        }

    private fun leafObjectAsEnumishFunction(leafObject: FirRegularClassSymbol) =
        createMemberFunction(
            leafObject,
            EnumizeKey,
            EnumizeNames.AS_ENUMISH,
            leafObject.defaultType(),
        ) {
            status { isOverride = true }
        }

    // 返り値型は docs/コンパイラプラグイン設計01.md §5.4 の規則で決める。interface の場合は default 実装（ボディは IR が充填）
    private fun leafClassAsEnumishFunction(
        leaf: FirRegularClassSymbol,
        base: FirRegularClassSymbol,
    ) =
        createMemberFunction(
            leaf,
            EnumizeKey,
            EnumizeNames.AS_ENUMISH,
            resolver.asEnumishReturnType(leaf, base),
        ) {
            modality = if (leaf.classKind == ClassKind.INTERFACE) Modality.OPEN else Modality.FINAL
            status { isOverride = true }
        }
}
