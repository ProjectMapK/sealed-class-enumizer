// referenceClass / referenceFunctions 等は 2.4 系で deprecated だが後継（finderForSource 等）は
// まだ安定しておらず、ClassId / CallableId からの静的参照という設計意図（設計02 §4.2）に合致するため使用を続ける
@file:OptIn(UnsafeDuringIrConstructionAPI::class)
@file:Suppress("DEPRECATION")

package org.wrongwrong.sealedClassEnumizer.compiler.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeKey
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames

// IR 生成で共有する参照（runtime-api のシンボル・ビルトイン型）。すべて ClassId / CallableId からの
// 静的参照であり、名前解決を経ない（設計02 §4.2）
class EnumizeIrContext(val pluginContext: IrPluginContext) {
    val generatedOrigin: IrDeclarationOrigin = IrDeclarationOrigin.GeneratedByPlugin(EnumizeKey)

    val holderBaseClass: IrClassSymbol =
        pluginContext.referenceClass(EnumizeNames.ENTRIES_HOLDER_BASE_CLASS_ID)
            ?: error("runtime-api class not found on classpath: ${EnumizeNames.ENTRIES_HOLDER_BASE_CLASS_ID}")

    val holderBaseConstructor = pluginContext.referenceConstructors(EnumizeNames.ENTRIES_HOLDER_BASE_CLASS_ID)
        .single()

    val holderEntriesProperty: IrPropertySymbol = holderProperty(EnumizeNames.ENTRIES)

    val holderEnumizedRootClassProperty: IrPropertySymbol = holderProperty(EnumizeNames.ENUMIZED_ROOT_CLASS)

    val holderCreateEntries: IrSimpleFunctionSymbol = holderFunction(EnumizeNames.CREATE_ENTRIES)

    val holderGetByLabel: IrSimpleFunctionSymbol = holderFunction(EnumizeNames.GET_BY_LABEL)

    val holderGetByLabelOrNull: IrSimpleFunctionSymbol = holderFunction(EnumizeNames.GET_BY_LABEL_OR_NULL)

    val listOfVararg: IrSimpleFunctionSymbol = pluginContext
        .referenceFunctions(CallableId(FqName("kotlin.collections"), Name.identifier("listOf")))
        .single { symbol ->
            symbol.owner.parameters.size == 1 && symbol.owner.parameters.single().varargElementType != null
        }

    val stringType: IrType = pluginContext.irBuiltIns.stringType

    val unitType: IrType = pluginContext.irBuiltIns.unitType

    val anyClass: IrClassSymbol = pluginContext.irBuiltIns.anyClass

    val anyToString: IrSimpleFunctionSymbol = anyClass.owner.declarations
        .filterIsInstance<IrSimpleFunction>()
        .single { it.name == EnumizeNames.TO_STRING && it.parameters.size == 1 }
        .symbol

    fun kClassTypeOf(argument: IrType): IrType = pluginContext.irBuiltIns.kClassClass.typeWith(argument)

    fun listTypeOf(argument: IrType): IrType = pluginContext.irBuiltIns.listClass.typeWith(argument)

    fun builder(symbol: IrSymbol): DeclarationIrBuilder = DeclarationIrBuilder(pluginContext, symbol)

    fun isOurs(declaration: IrDeclaration): Boolean =
        (declaration.origin as? IrDeclarationOrigin.GeneratedByPlugin)?.pluginKey == EnumizeKey

    private fun holderProperty(name: Name): IrPropertySymbol =
        pluginContext.referenceProperties(CallableId(EnumizeNames.ENTRIES_HOLDER_BASE_CLASS_ID, name)).single()

    private fun holderFunction(name: Name): IrSimpleFunctionSymbol =
        pluginContext.referenceFunctions(CallableId(EnumizeNames.ENTRIES_HOLDER_BASE_CLASS_ID, name)).single()
}
