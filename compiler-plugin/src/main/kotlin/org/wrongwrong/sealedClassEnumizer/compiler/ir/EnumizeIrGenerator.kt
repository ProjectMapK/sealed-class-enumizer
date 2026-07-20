@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package org.wrongwrong.sealedClassEnumizer.compiler.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObjectValue
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.name.Name
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames

// 階層単位の IR 生成（設計02 §1・§5）: EntriesHolder の生成、FIR 生成メンバーのボディ充填、
// kind の toString の IR-only 生成。toString を除き、生成の有無の判定は FIR 側にある —
// IR は FIR が宣言したメンバーを埋めるだけであり、充填対象を独自に増減させない
class EnumizeIrGenerator(pluginContext: IrPluginContext) {
    private val ctx = EnumizeIrContext(pluginContext)
    private val holderGenerator = EnumizeEntriesHolderIrGenerator(ctx)

    fun processHierarchy(base: IrClass) {
        val enumish = base.declarations.filterIsInstance<IrClass>()
            .firstOrNull { ctx.isOurs(it) && it.name == EnumizeNames.ENUMISH_NAME }
            ?: return
        val companion = enumish.companionObject() ?: return
        val leaves = collectLeaves(base)
        val kinds = leaves.mapNotNull(::kindOf)
        val holder = holderGenerator.generate(enumish, base, kinds)
        ensureObjectConstructorBody(companion)
        fillEnumishCompanionProperty(enumish, companion)
        fillCompanionMembers(companion, holder)
        leaves.forEach { leaf -> fillLeafMembers(leaf) }
        kinds.forEach { kind ->
            ensureObjectConstructorBody(kind)
            generateToStringIfNeeded(kind)
        }
    }

    // 末端の列挙（設計02 §2）: コンパイラの継承者リスト（FQN 順に正規化済み）を走査し、
    // 中間 sealed に到達したらその継承者リストへ再帰的に降りる。並べ替えは一切行わない（§3）
    private fun collectLeaves(base: IrClass): List<IrClass> {
        val result = mutableListOf<IrClass>()
        collectLeavesInto(base, result, LinkedHashSet())
        return result
    }

    private fun collectLeavesInto(current: IrClass, result: MutableList<IrClass>, visited: MutableSet<IrClass>) {
        if (!visited.add(current)) return
        for (subclassSymbol in current.sealedSubclasses) {
            val subclass = subclassSymbol.owner
            if (subclass.modality == Modality.SEALED) {
                collectLeavesInto(subclass, result, visited)
            } else {
                result.add(subclass)
            }
        }
    }

    private fun kindOf(leaf: IrClass): IrClass? =
        if (leaf.kind == ClassKind.OBJECT) leaf else leaf.companionObject()

    // ---- 基底ファイル帰属のボディ（設計02 §5.1） ----

    private fun fillEnumishCompanionProperty(enumish: IrClass, companion: IrClass) {
        val getter = ourPropertyGetter(enumish, EnumizeNames.ENUMISH_COMPANION_PROPERTY) ?: return
        getter.body = ctx.builder(getter.symbol).run {
            irBlockBody {
                +irReturn(irGetObjectValue(companion.defaultType, companion.symbol))
            }
        }
    }

    private fun fillCompanionMembers(companion: IrClass, holder: IrClass) {
        ourPropertyGetter(companion, EnumizeNames.ENTRIES)?.let { getter ->
            fillHolderDelegatingGetter(getter, holder)
        }
        ourFunction(companion, EnumizeNames.VALUE_OF)?.let { function ->
            fillHolderDelegatingFunction(function, holder, ctx.holderGetByLabel)
        }
        ourFunction(companion, EnumizeNames.VALUE_OF_OR_NULL)?.let { function ->
            fillHolderDelegatingFunction(function, holder, ctx.holderGetByLabelOrNull)
        }
    }

    private fun fillHolderDelegatingGetter(getter: IrSimpleFunction, holder: IrClass) {
        val holderEntriesGetter = ctx.holderEntriesProperty.owner.getter
            ?: error("EnumishEntriesHolder.entries getter not found")
        getter.body = ctx.builder(getter.symbol).run {
            irBlockBody {
                +irReturn(
                    irCall(holderEntriesGetter.symbol, getter.returnType).apply {
                        dispatchReceiver = irGetObjectValue(holder.defaultType, holder.symbol)
                    }
                )
            }
        }
    }

    private fun fillHolderDelegatingFunction(
        function: IrSimpleFunction,
        holder: IrClass,
        callee: IrSimpleFunctionSymbol,
    ) {
        val valueParameter = function.parameters.first { it.kind == IrParameterKind.Regular }
        function.body = ctx.builder(function.symbol).run {
            irBlockBody {
                +irReturn(
                    irCall(callee, function.returnType).apply {
                        dispatchReceiver = irGetObjectValue(holder.defaultType, holder.symbol)
                        setRegularArgument(callee, irGet(valueParameter))
                    }
                )
            }
        }
    }

    // ---- 末端側のファイル帰属のボディ（設計02 §5.2） ----

    private fun fillLeafMembers(leaf: IrClass) {
        if (leaf.kind == ClassKind.OBJECT) {
            fillKindMembers(kind = leaf, leaf = leaf)
            ourFunction(leaf, EnumizeNames.AS_ENUMISH)?.let { function ->
                function.body = ctx.builder(function.symbol).run {
                    irBlockBody {
                        val receiver = function.dispatchReceiverParameter
                            ?: error("dispatch receiver missing for asEnumish of ${leaf.name}")
                        +irReturn(irGet(receiver))
                    }
                }
            }
            return
        }
        val companion = leaf.companionObject()
        ourFunction(leaf, EnumizeNames.AS_ENUMISH)?.let { function ->
            val kind = companion ?: return@let
            function.body = ctx.builder(function.symbol).run {
                irBlockBody {
                    +irReturn(irGetObjectValue(kind.defaultType, kind.symbol))
                }
            }
        }
        if (companion != null) {
            fillKindMembers(kind = companion, leaf = leaf)
        }
    }

    private fun fillKindMembers(kind: IrClass, leaf: IrClass) {
        ourPropertyGetter(kind, EnumizeNames.LABEL)?.let { getter ->
            getter.body = ctx.builder(getter.symbol).run {
                irBlockBody {
                    // label は末端宣言の単純名（companion 自身が末端の場合はその宣言名 = leaf 自身）
                    +irReturn(irString(leaf.name.asString()))
                }
            }
        }
        ourPropertyGetter(kind, EnumizeNames.ENUMIZED_CLASS_PROPERTY)?.let { getter ->
            val leafStarType = leaf.symbol.starProjectedType
            getter.body = ctx.builder(getter.symbol).run {
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
    }

    // ---- kind の toString（IR-only・宣言ごと生成。設計02 §5.3） ----

    private fun generateToStringIfNeeded(kind: IrClass) {
        val toStrings = kind.declarations.filterIsInstance<IrSimpleFunction>().filter(::isPlainToString)
        // kind 自身の手動宣言・data object の言語合成（fake でない宣言）があれば生成しない（原則 1）
        if (toStrings.any { !it.isFakeOverride }) return
        // 継承経路上の最も派生側の toString が Any 以外の具象実装なら生成しない（原則 1）
        if (findInheritedConcreteToString(kind) != null) return
        val labelGetter = ourPropertyGetter(kind, EnumizeNames.LABEL) ?: return
        val fakeOverrides = toStrings.filter { it.isFakeOverride }
        val overridden = fakeOverrides.firstOrNull()?.overriddenSymbols ?: listOf(ctx.anyToString)
        kind.declarations.removeAll(fakeOverrides)
        val function = kind.addFunction(
            name = EnumizeNames.TO_STRING.asString(),
            returnType = ctx.stringType,
            modality = Modality.OPEN,
            origin = ctx.generatedOrigin,
        )
        function.overriddenSymbols = overridden
        function.body = ctx.builder(function.symbol).run {
            irBlockBody {
                val receiver = function.dispatchReceiverParameter
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
            val declared = current.declarations.filterIsInstance<IrSimpleFunction>()
                .firstOrNull { isPlainToString(it) && !it.isFakeOverride }
            if (declared != null) {
                return declared.takeIf { it.modality != Modality.ABSTRACT }
            }
            current = superclassOf(current)
        }
        return null
    }

    private fun superclassOf(irClass: IrClass): IrClass? =
        irClass.superTypes.mapNotNull { it.classOrNull?.owner }.firstOrNull { it.kind == ClassKind.CLASS }

    // ---- 共通 ----

    // 生成 companion のコンストラクタボディが Fir2Ir で充填されなかった場合の補完
    private fun ensureObjectConstructorBody(objectClass: IrClass) {
        if (!ctx.isOurs(objectClass)) return
        val constructor = objectClass.constructors.firstOrNull { it.isPrimary } ?: return
        if (constructor.body != null) return
        val anyConstructor = ctx.anyClass.owner.constructors.first()
        constructor.body = ctx.builder(constructor.symbol).run {
            irBlockBody {
                +irDelegatingConstructorCall(anyConstructor)
                +IrInstanceInitializerCallImpl(
                    UNDEFINED_OFFSET,
                    UNDEFINED_OFFSET,
                    objectClass.symbol,
                    ctx.unitType,
                )
            }
        }
    }

    // 生成プロパティはボディを IR で充填する getter-only であり backing field を持たない。
    // FIR 側の宣言が default アクセサ形のため Fir2Ir が field を実体化することがあり、
    // interface 上ではそれが不正な class file（instance field）になるため、充填時に除去する
    private fun ourPropertyGetter(container: IrClass, name: Name): IrSimpleFunction? {
        val property = container.declarations.filterIsInstance<IrProperty>()
            .firstOrNull { ctx.isOurs(it) && it.name == name }
            ?: return null
        property.backingField = null
        return property.getter
    }

    private fun ourFunction(container: IrClass, name: Name): IrSimpleFunction? =
        container.declarations.filterIsInstance<IrSimpleFunction>()
            .firstOrNull { ctx.isOurs(it) && it.name == name }

    private fun IrMemberAccessExpression<*>.setRegularArgument(
        callee: IrSimpleFunctionSymbol,
        value: IrExpression,
    ) {
        val index = callee.owner.parameters.indexOfFirst { it.kind == IrParameterKind.Regular }
        arguments[index] = value
    }
}
