@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package io.github.projectmapk.sealedClassEnumizer.compiler.ir

import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeLabelCase
import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeNames
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObjectValue
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.Name

// 末端側のファイル帰属の IR 生成（docs/コンパイラプラグイン設計02.md §5.2・§5.3）: asEnumish / label / enumizedClass のボディ充填、
// 生成 companion のコンストラクタ補完、kind の toString の IR-only 生成。
// 充填対象は origin の刻印だけで決まり階層の走査を伴わないため、@Enumize 基底が IC ラウンドに
// 同席しなくても成立する（docs/コンパイラプラグイン設計00.md §5）。kind に対応する末端は、生成 enumizedClass の宣言型
// `KClass<末端>` から読む — FIR が確定させた対応をそのまま使い、IR 側で導出し直さない
class EnumizeIrLeafGenerator(private val ctx: EnumizeIrContext) {

    // 階層の labelCase は基底単位で決まるため、基底 IrClass ごとに一度だけ読み取って持ち回る
    // （kind ごとの充填で同じ基底のアノテーション読取・解析を繰り返さない。FIR 側の
    // labelCaseCache と対になるキャッシュ）。IC ラウンド外の基底 = 逆直列化クラスも同じ経路で
    // 解決するため、モジュール内の基底列挙による事前計算は採らない
    private val labelCaseByBase = HashMap<IrClass, EnumizeLabelCase>()

    fun process(irClass: IrClass) {
        if (isBaseSideGenerated(irClass)) return
        ctx.ensureObjectConstructorBody(irClass)
        fillAsEnumish(irClass)
        fillKindMembers(irClass)
    }

    // 生成 Enumish とその companion は基底ファイル帰属の生成物であり、充填は EnumizeIrBaseGenerator が担う。
    // 特に Enumish の enumizedClass は abstract のまま残す必要があり、kind のそれと取り違えてはならない
    private fun isBaseSideGenerated(irClass: IrClass): Boolean =
        isGeneratedEnumish(irClass) || irClass.parentClassOrNull?.let(::isGeneratedEnumish) == true

    private fun isGeneratedEnumish(irClass: IrClass): Boolean =
        irClass.isGeneratedByEnumize && irClass.name == EnumizeNames.ENUMISH_NAME

    // ---- 末端宣言に属するメンバー ----

    private fun fillAsEnumish(leaf: IrClass) {
        val function = ctx.ourFunction(leaf, EnumizeNames.AS_ENUMISH) ?: return
        function.body =
            ctx.builder(function.symbol).run {
                irBlockBody {
                    // 末端 object は自身が kind であり、レシーバをそのまま返す。
                    // それ以外の末端（class / enum class / interface）は自身の companion を返す
                    if (leaf.kind == ClassKind.OBJECT) {
                        val receiver =
                            function.dispatchReceiverParameter
                                ?: error("dispatch receiver missing for asEnumish of ${leaf.name}")
                        +irReturn(irGet(receiver))
                    } else {
                        val kind =
                            leaf.companionObject()
                                ?: error("companion object missing for asEnumish of ${leaf.name}")
                        +irReturn(irGetObjectValue(kind.defaultType, kind.symbol))
                    }
                }
            }
    }

    // ---- kind に属するメンバー ----

    // 生成 enumizedClass を持つクラスが kind であり、その宣言型が対応する末端を一意に決める
    private fun fillKindMembers(kind: IrClass) {
        val enumizedClassGetter =
            ctx.ourPropertyGetter(kind, EnumizeNames.ENUMIZED_CLASS_PROPERTY) ?: return
        val leaf = leafOf(enumizedClassGetter)
        fillEnumizedClass(enumizedClassGetter, leaf)
        fillLabel(kind, leaf)
        generateToStringIfNeeded(kind)
    }

    private fun leafOf(enumizedClassGetter: IrSimpleFunction): IrClass {
        val argument = (enumizedClassGetter.returnType as? IrSimpleType)?.arguments?.singleOrNull()
        return argument?.typeOrNull?.classOrNull?.owner
            ?: error(
                "enumizedClass of ${enumizedClassGetter.parentAsClass.name} does not denote KClass<leaf>"
            )
    }

    private fun fillEnumizedClass(getter: IrSimpleFunction, leaf: IrClass) {
        val leafStarType = leaf.symbol.starProjectedType
        getter.body =
            ctx.builder(getter.symbol).run {
                irBlockBody {
                    +irReturn(
                        IrClassReferenceImpl(
                            UNDEFINED_OFFSET,
                            UNDEFINED_OFFSET,
                            ctx.kClassTypeOf(leafStarType),
                            leaf.symbol,
                            leafStarType,
                        )
                    )
                }
            }
    }

    private fun fillLabel(kind: IrClass, leaf: IrClass) {
        val getter = ctx.ourPropertyGetter(kind, EnumizeNames.LABEL) ?: return
        val label = labelOf(leaf)
        getter.body = ctx.builder(getter.symbol).run { irBlockBody { +irReturn(irString(label)) } }
    }

    // ---- 最終 label の決定（docs/概要.md §4。FIR チェッカー側の EnumizeHierarchyResolver.labelOf と同じ規則） ----

    // 明示指定（@EnumishLabel。ケース変換は適用しない） > 階層の labelCase による変換。
    // 変換の入力は末端宣言の単純名（companion 自身が末端の場合はその宣言名 = leaf 自身）
    private fun labelOf(leaf: IrClass): String =
        explicitLabelOf(leaf) ?: labelCaseOf(leaf).convert(leaf.name.asString())

    private fun explicitLabelOf(leaf: IrClass): String? =
        leaf
            .getAnnotation(EnumizeNames.ENUMISH_LABEL_ANNOTATION_FQ_NAME)
            ?.let { annotationArgument(it, EnumizeNames.VALUE_PARAMETER) }
            ?.let { (it as? IrConst)?.value as? String }

    // 基底は末端の supertype 閉包から探す — 末端は基底に依存するため、基底が IC ラウンドに
    // 同席しなくても逆直列化された宣言（アノテーション込み）へ到達できる
    private fun labelCaseOf(leaf: IrClass): EnumizeLabelCase {
        val base = enumizeBaseOf(leaf) ?: return ctx.defaultLabelCase
        return labelCaseByBase.getOrPut(base) { readLabelCase(base) }
    }

    // @Enumize の labelCase の読み取り（基底ごとに一度だけ呼ばれる）。引数未指定・PROJECT_DEFAULT・
    // 未知のエントリ名（runtime-api との版ずれ）はいずれもプロジェクト既定（ctx.defaultLabelCase）へ倒す
    private fun readLabelCase(base: IrClass): EnumizeLabelCase {
        val annotation = base.getAnnotation(EnumizeNames.ENUMIZE_ANNOTATION_FQ_NAME)
        val entryName =
            annotation
                ?.let { annotationArgument(it, EnumizeNames.LABEL_CASE_PARAMETER) }
                ?.let { (it as? IrGetEnumValue)?.symbol?.owner?.name }
        return entryName?.let { EnumizeLabelCase.fromNameOrNull(it.asString()) }
            ?: ctx.defaultLabelCase
    }

    // 末端から supertype を上向きに辿り、@Enumize の付いたクラスを探す。複数基底への所属は
    // MULTIPLE_HIERARCHIES エラーで IR まで到達しないため、最初の 1 件で確定してよい
    private fun enumizeBaseOf(leaf: IrClass): IrClass? = findEnumizeBase(leaf, HashSet())

    private fun findEnumizeBase(current: IrClass, visited: MutableSet<IrClass>): IrClass? =
        when {
            !visited.add(current) -> null
            current.hasAnnotation(EnumizeNames.ENUMIZE_ANNOTATION_FQ_NAME) -> current
            else ->
                current.superTypes.firstNotNullOfOrNull { supertype ->
                    supertype.classOrNull?.owner?.let { findEnumizeBase(it, visited) }
                }
        }

    // 引数列は宣言側パラメータ列と 1:1 対応で並ぶ（省略された既定値引数は null）
    private fun annotationArgument(call: IrConstructorCall, parameter: Name): IrExpression? {
        val index =
            call.symbol.owner.parameters.indexOfFirst {
                it.kind == IrParameterKind.Regular && it.name == parameter
            }
        return if (index >= 0) call.arguments.getOrNull(index) else null
    }

    // ---- kind の toString（IR-only・宣言ごと生成。docs/コンパイラプラグイン設計02.md §5.3） ----

    private fun generateToStringIfNeeded(kind: IrClass) {
        val toStrings =
            kind.declarations.filterIsInstance<IrSimpleFunction>().filter(::isPlainToString)
        // kind 自身の手動宣言・data object の言語合成（fake でない宣言）があれば生成しない（原則 1）
        if (toStrings.any { !it.isFakeOverride }) return
        // 継承経路上の最も派生側の toString が Any 以外の具象実装なら生成しない（原則 1）
        if (findInheritedConcreteToString(kind) != null) return
        val labelGetter = ctx.ourPropertyGetter(kind, EnumizeNames.LABEL) ?: return
        val fakeOverrides = toStrings.filter { it.isFakeOverride }
        val overridden = fakeOverrides.firstOrNull()?.overriddenSymbols ?: listOf(ctx.anyToString)
        kind.declarations.removeAll(fakeOverrides)
        val function =
            kind.addFunction(
                name = EnumizeNames.TO_STRING.asString(),
                returnType = ctx.stringType,
                modality = Modality.OPEN,
                origin = ctx.generatedOrigin,
            )
        function.overriddenSymbols = overridden
        function.body =
            ctx.builder(function.symbol).run {
                irBlockBody {
                    val receiver =
                        function.dispatchReceiverParameter
                            ?: error("dispatch receiver missing for toString of ${kind.name}")
                    +irReturn(
                        irCall(labelGetter.symbol, ctx.stringType).apply {
                            dispatchReceiver = irGet(receiver)
                        }
                    )
                }
            }
    }

    private fun isPlainToString(function: IrSimpleFunction): Boolean =
        function.name == EnumizeNames.TO_STRING &&
            function.parameters.all { it.kind == IrParameterKind.DispatchReceiver } &&
            function.typeParameters.isEmpty()

    // superclass 連鎖を派生側から辿り、最初に見つかる toString 宣言が Any 以外の具象実装ならそれを返す
    private fun findInheritedConcreteToString(kind: IrClass): IrSimpleFunction? {
        var current = superclassOf(kind)
        while (current != null && current.symbol != ctx.anyClass) {
            val declared =
                current.declarations.filterIsInstance<IrSimpleFunction>().firstOrNull {
                    isPlainToString(it) && !it.isFakeOverride
                }
            if (declared != null) {
                return declared.takeIf { it.modality != Modality.ABSTRACT }
            }
            current = superclassOf(current)
        }
        return null
    }

    private fun superclassOf(irClass: IrClass): IrClass? =
        irClass.superTypes
            .mapNotNull { it.classOrNull?.owner }
            .firstOrNull { it.kind == ClassKind.CLASS }
}
