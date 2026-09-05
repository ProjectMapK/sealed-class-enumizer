@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package io.github.projectmapk.sealedClassEnumizer.compiler.ir

import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeNames
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
import org.jetbrains.kotlin.ir.util.kotlinFqName

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
        val kinds = collectLeaves(base).map(::kindOf)
        // 参照不能 kind には IR-only アクセサを生成し、createEntries はその取得式ビルダで組み立てる（docs/コンパイラプラグイン設計02.md §4.3）
        val kindProviders = accessorGenerator.buildKindProviders(base, enumish, kinds)
        val holder = holderGenerator.generate(enumish, base, kindProviders)
        ctx.ensureObjectConstructorBody(companion)
        fillEnumishCompanionProperty(enumish, companion)
        fillCompanionMembers(companion, holder)
    }

    // 末端の列挙（docs/コンパイラプラグイン設計02.md §2）: コンパイラの継承者リスト（FQN 順に正規化済み）を走査し、
    // 中間 sealed に到達したらその継承者リストへ再帰的に降りる。並べ替えは一切行わない（docs/コンパイラプラグイン設計02.md §3）。
    // 中間 sealed を含む全メンバーを走査順を保つ集合へ初出のみ入れ、そこから末端だけを取り出す。
    // 複数の中間 sealed を同時に実装するメンバーは経路の数だけ到達するため、この集合が
    // 末端の重複掲載（kind の集合が壊れる）と中間の再展開（同じ部分木の再走査）を同時に抑える
    private fun collectLeaves(base: IrClass): List<IrClass> {
        val members = LinkedHashSet<IrClass>()
        collectMembersInto(base, members)
        return members.filterNot { it.modality == Modality.SEALED }
    }

    private fun collectMembersInto(current: IrClass, members: MutableSet<IrClass>) {
        for (subclassSymbol in current.sealedSubclasses) {
            val subclass = subclassSymbol.owner
            if (!members.add(subclass)) continue
            if (subclass.modality == Modality.SEALED) {
                collectMembersInto(subclass, members)
            }
        }
    }

    // 末端 class の kind は companion であり、FIR が手動宣言の流用か自動生成のどちらかで必ず用意する
    // （docs/コンパイラプラグイン設計01.md §6.2）。欠けたまま進むと entries が静かに不完全になるため、ここで失敗させる
    private fun kindOf(leaf: IrClass): IrClass =
        if (leaf.kind == ClassKind.OBJECT) leaf
        else
            leaf.companionObject()
                ?: error("companion object missing for leaf ${leaf.kotlinFqName}")

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
