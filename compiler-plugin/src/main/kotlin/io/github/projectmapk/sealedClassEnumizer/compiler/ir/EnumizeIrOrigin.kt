package io.github.projectmapk.sealedClassEnumizer.compiler.ir

import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeKey
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin

// 自プラグインが生成した宣言か（origin の刻印による判定。docs/コンパイラプラグイン設計02.md §1）。
// IC ラウンド外のファイル由来の宣言は逆直列化で刻印を失うため、このラウンドで FIR が生成した
// 宣言だけが true になる。IR 側の充填対象はこの判定だけで決まり、階層の走査を必要としない
val IrDeclaration.isGeneratedByEnumize: Boolean
    get() = (origin as? IrDeclarationOrigin.GeneratedByPlugin)?.pluginKey == EnumizeKey
