@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package org.wrongwrong.sealedClassEnumizer.compiler.ir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObjectValue
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.defaultType
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames

// 基底ファイル帰属の IR 生成（docs/コンパイラプラグイン設計02.md §4・§5.1）: $EntriesHolder の生成と、生成 Enumish・
// その companion のボディ充填。継承者の集合に依存する生成物はここでのみ作られ、基底が IC ラウンドに
// 同席する場合に限って走る（P1 の帰属を保つ）。末端側のボディは基底の同席に依らず
// EnumizeIrLeafGenerator が充填する
class EnumizeIrBaseGenerator(private val ctx: EnumizeIrContext) {
    private val holderGenerator = EnumizeEntriesHolderIrGenerator(ctx)
    private val accessorGenerator = EnumizeKindAccessorIrGenerator(ctx)

    fun process(base: IrClass) {
        val enumish =
            base.declarations.filterIsInstance<IrClass>().firstOrNull {
                it.isGeneratedByEnumize && it.name == EnumizeNames.ENUMISH_NAME
            } ?: return
        val companion = enumish.companionObject() ?: return
        val kinds = collectLeaves(base).mapNotNull(::kindOf)
        // 参照不能 kind には IR-only アクセサを生成し、createEntries はその取得式ビルダで組み立てる（docs/コンパイラプラグイン設計02.md §4.3）
        val kindProviders = accessorGenerator.buildKindProviders(base, enumish, kinds)
        val holder = holderGenerator.generate(enumish, base, kindProviders)
        ctx.ensureObjectConstructorBody(companion)
        fillEnumishCompanionProperty(enumish, companion)
        fillCompanionMembers(companion, holder)
    }

    // 末端の列挙（docs/コンパイラプラグイン設計02.md §2）: コンパイラの継承者リスト（FQN 順に正規化済み）を走査し、
    // 中間 sealed に到達したらその継承者リストへ再帰的に降りる。並べ替えは一切行わない（§3）
    private fun collectLeaves(base: IrClass): List<IrClass> {
        val result = mutableListOf<IrClass>()
        collectLeavesInto(base, result, LinkedHashSet())
        return result
    }

    private fun collectLeavesInto(
        current: IrClass,
        result: MutableList<IrClass>,
        visited: MutableSet<IrClass>,
    ) {
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

    // ---- 生成 Enumish とその companion のボディ（docs/コンパイラプラグイン設計02.md §5.1） ----

    private fun fillEnumishCompanionProperty(enumish: IrClass, companion: IrClass) {
        val getter =
            ctx.ourPropertyGetter(enumish, EnumizeNames.ENUMISH_COMPANION_PROPERTY) ?: return
        getter.body =
            ctx.builder(getter.symbol).run {
                irBlockBody { +irReturn(irGetObjectValue(companion.defaultType, companion.symbol)) }
            }
    }

    private fun fillCompanionMembers(companion: IrClass, holder: IrClass) {
        ctx.ourPropertyGetter(companion, EnumizeNames.ENTRIES)?.let { getter ->
            fillHolderDelegatingGetter(getter, holder)
        }
        ctx.ourFunction(companion, EnumizeNames.VALUE_OF)?.let { function ->
            fillHolderDelegatingFunction(function, holder, ctx.holderGetByLabel)
        }
        ctx.ourFunction(companion, EnumizeNames.VALUE_OF_OR_NULL)?.let { function ->
            fillHolderDelegatingFunction(function, holder, ctx.holderGetByLabelOrNull)
        }
    }

    private fun fillHolderDelegatingGetter(getter: IrSimpleFunction, holder: IrClass) {
        val holderEntriesGetter =
            ctx.holderEntriesProperty.owner.getter
                ?: error("EnumishEntriesHolder.entries getter not found")
        getter.body =
            ctx.builder(getter.symbol).run {
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
        function.body =
            ctx.builder(function.symbol).run {
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

    private fun IrMemberAccessExpression<*>.setRegularArgument(
        callee: IrSimpleFunctionSymbol,
        value: IrExpression,
    ) {
        val index = callee.owner.parameters.indexOfFirst { it.kind == IrParameterKind.Regular }
        arguments[index] = value
    }
}
