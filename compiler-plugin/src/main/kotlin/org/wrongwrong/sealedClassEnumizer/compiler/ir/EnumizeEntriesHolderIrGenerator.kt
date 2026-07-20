@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package org.wrongwrong.sealedClassEnumizer.compiler.ir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addGetter
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGetObjectValue
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.createThisReceiverParameter
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.ir.util.defaultType
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames

// $EntriesHolder の IR-only 生成（設計02 §4）。生成するのは supertype の指定・enumizedRootClass の実装・
// createEntries() の実装の 3 つだけであり、遅延初期化・探索・失敗メッセージは runtime-api の
// EnumishEntriesHolder 側が持つ。IR-only のためメタデータに載らず、Kotlin ソースからは不可視（ABI 保証外）
class EnumizeEntriesHolderIrGenerator(private val ctx: EnumizeIrContext) {

    // enumish の中にネストして生成し、IR 上の帰属を基底ファイルにする（P1 の実装）
    fun generate(enumish: IrClass, base: IrClass, kinds: List<IrClass>): IrClass {
        val holder = createHolderClass(enumish)
        generateConstructor(holder, enumish)
        generateEnumizedRootClassOverride(holder, base)
        generateCreateEntriesOverride(holder, enumish, kinds)
        return holder
    }

    private fun createHolderClass(enumish: IrClass): IrClass {
        val holder = ctx.pluginContext.irFactory.buildClass {
            name = EnumizeNames.ENTRIES_HOLDER_NAME
            kind = ClassKind.OBJECT
            modality = Modality.FINAL
            visibility = DescriptorVisibilities.PUBLIC
            origin = ctx.generatedOrigin
        }
        holder.parent = enumish
        enumish.declarations.add(holder)
        holder.createThisReceiverParameter()
        holder.superTypes = listOf(ctx.holderBaseClass.typeWith(enumish.defaultType))
        return holder
    }

    private fun generateConstructor(holder: IrClass, enumish: IrClass) {
        val constructor = holder.addConstructor {
            isPrimary = true
            visibility = DescriptorVisibilities.PRIVATE
            origin = ctx.generatedOrigin
        }
        fillObjectConstructorBody(constructor, holder) {
            irDelegatingConstructorCall(ctx.holderBaseConstructor.owner).apply {
                if (typeArguments.isNotEmpty()) {
                    typeArguments[0] = enumish.defaultType
                }
            }
        }
    }

    private fun generateEnumizedRootClassOverride(holder: IrClass, base: IrClass) {
        val baseStarType = base.symbol.starProjectedType
        val returnType = ctx.kClassTypeOf(baseStarType)
        val property = holder.addProperty {
            name = EnumizeNames.ENUMIZED_ROOT_CLASS
            visibility = DescriptorVisibilities.PROTECTED
            origin = ctx.generatedOrigin
        }
        property.overriddenSymbols = listOf(ctx.holderEnumizedRootClassProperty)
        val getter = property.addGetter {
            this.returnType = returnType
            visibility = DescriptorVisibilities.PROTECTED
            origin = ctx.generatedOrigin
        }
        val thisReceiver = holder.thisReceiver ?: error("thisReceiver missing for \$EntriesHolder")
        getter.parameters = listOf(thisReceiver.copyTo(getter, type = holder.defaultType))
        getter.overriddenSymbols = listOfNotNull(ctx.holderEnumizedRootClassProperty.owner.getter?.symbol)
        getter.body = ctx.builder(getter.symbol).run {
            irBlockBody {
                +irReturn(
                    IrClassReferenceImpl(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        returnType,
                        base.symbol,
                        baseStarType,
                    )
                )
            }
        }
    }

    private fun generateCreateEntriesOverride(holder: IrClass, enumish: IrClass, kinds: List<IrClass>) {
        val enumishType = enumish.defaultType
        val returnType = ctx.listTypeOf(enumishType)
        val function = holder.addFunction(
            name = EnumizeNames.CREATE_ENTRIES.asString(),
            returnType = returnType,
            modality = Modality.FINAL,
            visibility = DescriptorVisibilities.PROTECTED,
            origin = ctx.generatedOrigin,
        )
        function.overriddenSymbols = listOf(ctx.holderCreateEntries)
        function.body = ctx.builder(function.symbol).run {
            irBlockBody {
                // §3 の順序（コンパイラ提供の継承者リストの走査順）をそのまま listOf(...) の並びにする
                val vararg = IrVarargImpl(
                    UNDEFINED_OFFSET,
                    UNDEFINED_OFFSET,
                    ctx.pluginContext.irBuiltIns.arrayClass.typeWith(enumishType),
                    enumishType,
                    kinds.map { kind -> irGetObjectValue(kind.defaultType, kind.symbol) },
                )
                +irReturn(
                    irCall(ctx.listOfVararg, returnType).apply {
                        typeArguments[0] = enumishType
                        arguments[0] = vararg
                    }
                )
            }
        }
    }

    // createDefaultPrivateConstructor 由来のボディが Fir2Ir で欠けた場合の保険も兼ねた共通処理
    private fun fillObjectConstructorBody(
        constructor: IrConstructor,
        owner: IrClass,
        delegatingCall: IrBuilderWithScope.() -> IrExpression,
    ) {
        constructor.body = ctx.builder(constructor.symbol).run {
            irBlockBody {
                +delegatingCall()
                +IrInstanceInitializerCallImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, owner.symbol, ctx.unitType)
            }
        }
    }
}
