@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package org.wrongwrong.sealedClassEnumizer.compiler.ir

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames

// IR 側のエントリポイント（設計02 §1）。@Enumize 付き基底を収集し、階層単位で
// EntriesHolder 生成・ボディ充填・kind toString 生成を行う
class EnumizeIrGenerationExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val bases = collectEnumizeBases(moduleFragment)
        if (bases.isEmpty()) return
        val generator = EnumizeIrGenerator(pluginContext)
        bases.forEach(generator::processHierarchy)
    }

    private fun collectEnumizeBases(moduleFragment: IrModuleFragment): List<IrClass> {
        val result = mutableListOf<IrClass>()
        moduleFragment.files.forEach { file -> collectFrom(file, result) }
        return result
    }

    private fun collectFrom(container: IrDeclarationContainer, result: MutableList<IrClass>) {
        for (declaration in container.declarations) {
            if (declaration !is IrClass) continue
            collectIfEnumizeBase(declaration, result)
            collectFrom(declaration, result)
        }
    }

    private fun collectIfEnumizeBase(irClass: IrClass, result: MutableList<IrClass>) {
        if (irClass.hasAnnotation(EnumizeNames.ENUMIZE_ANNOTATION_FQ_NAME)) {
            result.add(irClass)
        }
    }
}
