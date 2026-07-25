@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package org.wrongwrong.sealedClassEnumizer.compiler.ir

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeNames

// IR 側のエントリポイント（docs/コンパイラプラグイン設計02.md §1）。ボディの充填は origin 駆動であり、FIR が生成した宣言を
// その持ち主のクラスごとに埋める。@Enumize 基底が IC ラウンドに同席しなくても末端側は成立する（docs/コンパイラプラグイン設計00.md §5）。
// 継承者の集合に依存する生成物（$EntriesHolder）と、それへ委譲する生成 companion のボディは、
// 基底が同席するラウンドに限り基底ファイル帰属で生成する（P1）
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
        generatedOwners.forEach(::verifyBodiesFilled)
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

    // 「FIR が生成した宣言はこのラウンドですべて充填される」ことは origin 駆動の不変条件である。
    // 取りこぼしはバックエンドの `Function has no body` として遠くで表面化するため、ここで検出して
    // どの宣言が漏れたかを名指しする（FIR 側が生成先を増やしたときの検知点でもある）
    private fun verifyBodiesFilled(owner: IrClass) {
        for (declaration in owner.declarations) {
            if (!declaration.isGeneratedByEnumize) continue
            when (declaration) {
                is IrProperty ->
                    declaration.getter?.let { verifyBody(it, declaration.modality, owner) }
                is IrSimpleFunction -> verifyBody(declaration, declaration.modality, owner)
                is IrConstructor -> verifyBody(declaration, Modality.FINAL, owner)
                else -> Unit
            }
        }
    }

    private fun verifyBody(function: IrFunction, modality: Modality, owner: IrClass) {
        if (modality == Modality.ABSTRACT || function.body != null) return
        error(
            "generated declaration was not filled: ${owner.kotlinFqName}.${function.name} (${function.origin})"
        )
    }
}
