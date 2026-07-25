package org.wrongwrong.sealedClassEnumizer.compiler.ir

import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.wrongwrong.sealedClassEnumizer.compiler.EnumizeKey

// 自プラグインが生成した宣言か（origin の刻印による判定。設計02 §1）。
// IC ラウンド外のファイル由来の宣言は逆直列化で刻印を失うため、このラウンドで FIR が生成した
// 宣言だけが true になる。IR 側の充填対象はこの判定だけで決まり、階層の走査を必要としない
val IrDeclaration.isGeneratedByEnumize: Boolean
    get() = (origin as? IrDeclarationOrigin.GeneratedByPlugin)?.pluginKey == EnumizeKey
