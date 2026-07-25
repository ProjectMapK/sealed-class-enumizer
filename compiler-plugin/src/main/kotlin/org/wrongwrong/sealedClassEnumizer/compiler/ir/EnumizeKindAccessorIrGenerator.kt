@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package org.wrongwrong.sealedClassEnumizer.compiler.ir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGetObjectValue
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.createThisReceiverParameter
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.fileOrNull
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.Name
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames

// createEntries が 1 つの kind を取得する式を組み立てるビルダ（呼び出し時の receiver は createEntries のビルダ）
typealias EnumizeKindProvider = IrBuilderWithScope.() -> IrExpression

// 参照不能 kind 用の IR-only アクセサ生成（docs/コンパイラプラグイン設計02.md §4.3）。基底本体スコープから名前参照できない kind
// （private/protected な既存 companion・別ファイルの private トップレベル末端・private 外側クラスの末端）へ、
// アクセス権を持つ位置に IR-only の internal アクセサを生成し、createEntries から呼んで実体を得る。
// FIR には宣言を作らないため（メタデータ非搭載）Kotlin ソースからは不可視であり、外部からの参照はできない。
// 参照可能な kind（public / internal・基底内ネストの private 末端）はアクセサを介さず直接 IrGetObjectValue で取得する
class EnumizeKindAccessorIrGenerator(private val ctx: EnumizeIrContext) {

    // kind ごとに「取得式ビルダ」を返す。参照不能な kind に対してはここでアクセサ宣言を生成する（副作用）
    fun buildKindProviders(
        base: IrClass,
        enumish: IrClass,
        kinds: List<IrClass>,
    ): List<EnumizeKindProvider> {
        val enumishType = enumish.defaultType
        return kinds.map { kind -> providerFor(kind, base, enumishType) }
    }

    private fun providerFor(
        kind: IrClass,
        base: IrClass,
        enumishType: IrType,
    ): EnumizeKindProvider {
        val wall = outermostWall(kind, base) ?: return directProvider(kind)
        val container = wall.parent
        return if (container is IrClass) {
            nestedAccessorProvider(kind, container, enumishType)
        } else {
            topLevelAccessorProvider(kind, wall, enumishType)
        }
    }

    // ---- 取得式ビルダ ----

    private fun directProvider(kind: IrClass): EnumizeKindProvider = {
        irGetObjectValue(kind.defaultType, kind.symbol)
    }

    private fun nestedAccessorProvider(
        kind: IrClass,
        container: IrClass,
        enumishType: IrType,
    ): EnumizeKindProvider {
        val getter = createNestedAccessor(kind, container, enumishType)
        val accessor = getter.parent as IrClass
        return {
            irCall(getter.symbol, enumishType).apply {
                dispatchReceiver = irGetObjectValue(accessor.defaultType, accessor.symbol)
            }
        }
    }

    private fun topLevelAccessorProvider(
        kind: IrClass,
        wall: IrClass,
        enumishType: IrType,
    ): EnumizeKindProvider {
        val accessor = createTopLevelAccessor(kind, wall, enumishType)
        return { irCall(accessor.symbol, enumishType) }
    }

    // ---- 参照可能性の判定（基底本体から kind を名前参照できるか） ----

    // kind の外側クラス連鎖を辿り、基底から不可視な最も外側のクラス（＝壁）を返す。壁が無ければ null（直接参照可）
    private fun outermostWall(kind: IrClass, base: IrClass): IrClass? {
        var wall: IrClass? = null
        var current: IrClass? = kind
        while (current != null) {
            if (!isReferenceableFromBase(current, base)) wall = current
            current = current.parentClassOrNull
        }
        return wall
    }

    private fun isReferenceableFromBase(target: IrClass, base: IrClass): Boolean =
        when (target.visibility) {
            DescriptorVisibilities.PRIVATE,
            DescriptorVisibilities.PRIVATE_TO_THIS,
            DescriptorVisibilities.PROTECTED -> declaringScopeContainsBase(target, base)
            DescriptorVisibilities.LOCAL -> false
            else -> true // public / internal は同一モジュールから参照可
        }

    // private / protected な target が基底から見えるのは、基底が target の宣言スコープの内側にある場合。
    // ネストなら基底が外側クラスの subtree 内、file-private なトップレベルなら基底が同一ファイル内
    private fun declaringScopeContainsBase(target: IrClass, base: IrClass): Boolean {
        val parent = target.parentClassOrNull
        return if (parent != null) baseIsWithin(base, parent) else sameFile(base, target)
    }

    private fun baseIsWithin(base: IrClass, container: IrClass): Boolean {
        var current: IrClass? = base
        while (current != null) {
            if (current == container) return true
            current = current.parentClassOrNull
        }
        return false
    }

    private fun sameFile(first: IrClass, second: IrClass): Boolean {
        val firstFile = first.fileOrNull
        return firstFile != null && firstFile === second.fileOrNull
    }

    // ---- アクセサ宣言の生成 ----

    // 末端クラス内にネストした IR-only object。外側クラスの private / protected companion を intra-scope で参照する
    private fun createNestedAccessor(
        target: IrClass,
        container: IrClass,
        enumishType: IrType,
    ): IrSimpleFunction {
        val accessor =
            ctx.pluginContext.irFactory.buildClass {
                name = EnumizeNames.KIND_ACCESSOR_NAME
                kind = ClassKind.OBJECT
                modality = Modality.FINAL
                visibility = DescriptorVisibilities.INTERNAL
                origin = ctx.generatedOrigin
            }
        accessor.parent = container
        container.declarations.add(accessor)
        accessor.createThisReceiverParameter()
        accessor.superTypes = listOf(ctx.anyType)
        generateAccessorConstructor(accessor)
        return generateAccessorGetter(accessor, target, enumishType)
    }

    private fun generateAccessorConstructor(accessor: IrClass) {
        val constructor = accessor.addConstructor {
            isPrimary = true
            visibility = DescriptorVisibilities.PRIVATE
            origin = ctx.generatedOrigin
        }
        constructor.body =
            ctx.builder(constructor.symbol).run {
                irBlockBody {
                    +irDelegatingConstructorCall(ctx.anyConstructor.owner)
                    +IrInstanceInitializerCallImpl(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        accessor.symbol,
                        ctx.unitType,
                    )
                }
            }
    }

    private fun generateAccessorGetter(
        accessor: IrClass,
        kind: IrClass,
        enumishType: IrType,
    ): IrSimpleFunction {
        val getter =
            accessor.addFunction(
                name = EnumizeNames.KIND_ACCESSOR_GET.asString(),
                returnType = enumishType,
                modality = Modality.FINAL,
                visibility = DescriptorVisibilities.INTERNAL,
                origin = ctx.generatedOrigin,
            )
        getter.body =
            ctx.builder(getter.symbol).run {
                irBlockBody { +irReturn(irGetObjectValue(kind.defaultType, kind.symbol)) }
            }
        return getter
    }

    // file-private な壁（private トップレベル末端・private 外側クラス）に対しては、
    // 壁と同一ファイルのトップレベル IR-only 関数から kind を参照する（file-private は同一ファイル内から見える）
    private fun createTopLevelAccessor(
        kind: IrClass,
        wall: IrClass,
        enumishType: IrType,
    ): IrSimpleFunction {
        val file = wall.fileOrNull ?: error("file missing for kind accessor of ${kind.name}")
        val accessor =
            ctx.pluginContext.irFactory.buildFun {
                name = topLevelAccessorName(kind)
                returnType = enumishType
                modality = Modality.FINAL
                visibility = DescriptorVisibilities.INTERNAL
                origin = ctx.generatedOrigin
            }
        accessor.parent = file
        file.declarations.add(accessor)
        accessor.body =
            ctx.builder(accessor.symbol).run {
                irBlockBody { +irReturn(irGetObjectValue(kind.defaultType, kind.symbol)) }
            }
        return accessor
    }

    // 同一ファイル内で一意にするため kind の外側クラス連鎖を `$` で連結した相対名を含める
    private fun topLevelAccessorName(kind: IrClass): Name {
        val segments = mutableListOf<String>()
        var current: IrClass? = kind
        while (current != null) {
            segments.add(current.name.asString())
            current = current.parentClassOrNull
        }
        val relative = segments.asReversed().joinToString(separator = "\$")
        return Name.identifier(EnumizeNames.KIND_ACCESSOR_TOP_LEVEL_PREFIX + relative)
    }
}
