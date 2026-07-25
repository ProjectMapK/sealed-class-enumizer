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

// IR 側のエントリポイント（設計02 §1）。ボディの充填は origin 駆動であり、FIR が生成した宣言を
// その持ち主のクラスごとに埋める。@Enumize 基底が IC ラウンドに同席しなくても末端側は成立する（設計00 §5）。
// 継承者の集合に依存する生成物（$EntriesHolder と生成 companion の委譲）は基底が同席するラウンドに限り、
// 基底ファイル帰属で生成する（P1）
class EnumizeIrGenerationExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        // 生成の副作用（kind アクセサの追加・toString の差し替え）が走査と干渉しないよう、対象をここで確定させる
        val classes = collectClasses(moduleFragment)
        val bases = classes.filter { it.hasAnnotation(EnumizeNames.ENUMIZE_ANNOTATION_FQ_NAME) }
        val generatedOwners = classes.filter { irClass ->
            irClass.declarations.any { it.isGeneratedByEnumize }
        }
        // 階層を含まないモジュールでは runtime-api の参照解決ごと省く
        if (bases.isEmpty() && generatedOwners.isEmpty()) return
        val ctx = EnumizeIrContext(pluginContext)
        val leafGenerator = EnumizeIrLeafGenerator(ctx)
        generatedOwners.forEach(leafGenerator::process)
        val baseGenerator = EnumizeIrBaseGenerator(ctx)
        bases.forEach(baseGenerator::process)
    }

    private fun collectClasses(moduleFragment: IrModuleFragment): List<IrClass> {
        val result = mutableListOf<IrClass>()
        moduleFragment.files.forEach { file -> collectFrom(file, result) }
        return result
    }

    private fun collectFrom(container: IrDeclarationContainer, result: MutableList<IrClass>) {
        for (declaration in container.declarations) {
            if (declaration !is IrClass) continue
            result.add(declaration)
            collectFrom(declaration, result)
        }
    }
}
