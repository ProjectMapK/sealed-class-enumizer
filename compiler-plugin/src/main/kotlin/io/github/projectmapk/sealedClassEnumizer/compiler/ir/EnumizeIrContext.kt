// referenceClass / referenceFunctions 等は 2.4 系で deprecated（後継は finderForBuiltins() /
// finderForSource(fromFile) が返す DeclarationFinder で、引数は同じ ClassId / CallableId。差は参照を
// IC の lookup へ記録する点にある）。参照先は runtime-api と stdlib の固定シンボルに限られ（docs/コンパイラプラグイン設計02.md §4.2）、
// ファイル単位の lookup 記録を必要としないため、旧 API のまま DEPRECATION を抑止する
@file:OptIn(UnsafeDuringIrConstructionAPI::class)
@file:Suppress("DEPRECATION")

package io.github.projectmapk.sealedClassEnumizer.compiler.ir

import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeKey
import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeLabelCase
import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeNames
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

// IR 生成で共有する参照（runtime-api のシンボル・ビルトイン型）と、プラグイン生成宣言の取り回し。
// 参照はすべて ClassId / CallableId からの静的参照であり、名前解決を経ない（docs/コンパイラプラグイン設計02.md §4.2）。
// defaultLabelCase はプロジェクト既定の label ケース（CLI オプション由来。docs/概要.md §4）
class EnumizeIrContext(val pluginContext: IrPluginContext, val defaultLabelCase: EnumizeLabelCase) {
    val generatedOrigin: IrDeclarationOrigin = IrDeclarationOrigin.GeneratedByPlugin(EnumizeKey)

    val holderBaseClass: IrClassSymbol =
        pluginContext.referenceClass(EnumizeNames.ENTRIES_HOLDER_BASE_CLASS_ID)
            ?: error(
                "runtime-api class not found on classpath: ${EnumizeNames.ENTRIES_HOLDER_BASE_CLASS_ID}"
            )

    val holderBaseConstructor =
        pluginContext.referenceConstructors(EnumizeNames.ENTRIES_HOLDER_BASE_CLASS_ID).single()

    val holderEntriesProperty: IrPropertySymbol = holderProperty(EnumizeNames.ENTRIES)

    val holderEnumizedRootClassProperty: IrPropertySymbol =
        holderProperty(EnumizeNames.ENUMIZED_ROOT_CLASS)

    val holderCreateEntries: IrSimpleFunctionSymbol = holderFunction(EnumizeNames.CREATE_ENTRIES)

    val holderGetByLabel: IrSimpleFunctionSymbol = holderFunction(EnumizeNames.GET_BY_LABEL)

    val holderGetByLabelOrNull: IrSimpleFunctionSymbol =
        holderFunction(EnumizeNames.GET_BY_LABEL_OR_NULL)

    val listOfVararg: IrSimpleFunctionSymbol =
        pluginContext
            .referenceFunctions(CallableId(FqName("kotlin.collections"), Name.identifier("listOf")))
            .single { symbol ->
                symbol.owner.parameters.size == 1 &&
                    symbol.owner.parameters.single().varargElementType != null
            }

    val stringType: IrType = pluginContext.irBuiltIns.stringType

    val unitType: IrType = pluginContext.irBuiltIns.unitType

    val anyClass: IrClassSymbol = pluginContext.irBuiltIns.anyClass

    val anyType: IrType = pluginContext.irBuiltIns.anyType

    val anyConstructor: IrConstructorSymbol = anyClass.owner.constructors.first().symbol

    val anyToString: IrSimpleFunctionSymbol =
        anyClass.owner.declarations
            .filterIsInstance<IrSimpleFunction>()
            .single { it.name == EnumizeNames.TO_STRING && it.parameters.size == 1 }
            .symbol

    fun kClassTypeOf(argument: IrType): IrType =
        pluginContext.irBuiltIns.kClassClass.typeWith(argument)

    fun listTypeOf(argument: IrType): IrType = pluginContext.irBuiltIns.listClass.typeWith(argument)

    fun builder(symbol: IrSymbol): DeclarationIrBuilder =
        DeclarationIrBuilder(pluginContext, symbol)

    // 生成 object のコンストラクタボディが Fir2Ir で充填されなかった場合の補完
    fun ensureObjectConstructorBody(objectClass: IrClass) {
        if (!objectClass.isGeneratedByEnumize) return
        val constructor = objectClass.constructors.firstOrNull { it.isPrimary } ?: return
        if (constructor.body != null) return
        constructor.body =
            builder(constructor.symbol).run {
                irBlockBody {
                    +irDelegatingConstructorCall(anyConstructor.owner)
                    +IrInstanceInitializerCallImpl(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        objectClass.symbol,
                        unitType,
                    )
                }
            }
    }

    // 生成プロパティはボディを IR で充填する getter-only であり backing field を持たない。
    // FIR 側の宣言が default アクセサ形のため Fir2Ir が field を実体化することがあり、
    // interface 上ではそれが不正な class file（instance field）になるため、充填時に除去する
    fun ourPropertyGetter(container: IrClass, name: Name): IrSimpleFunction? {
        val property =
            container.declarations.filterIsInstance<IrProperty>().firstOrNull {
                it.isGeneratedByEnumize && it.name == name
            } ?: return null
        property.backingField = null
        return property.getter
    }

    fun ourFunction(container: IrClass, name: Name): IrSimpleFunction? =
        container.declarations.filterIsInstance<IrSimpleFunction>().firstOrNull {
            it.isGeneratedByEnumize && it.name == name
        }

    private fun holderProperty(name: Name): IrPropertySymbol =
        pluginContext
            .referenceProperties(CallableId(EnumizeNames.ENTRIES_HOLDER_BASE_CLASS_ID, name))
            .single()

    private fun holderFunction(name: Name): IrSimpleFunctionSymbol =
        pluginContext
            .referenceFunctions(CallableId(EnumizeNames.ENTRIES_HOLDER_BASE_CLASS_ID, name))
            .single()
}
