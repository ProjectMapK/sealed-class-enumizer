package io.github.projectmapk.sealedClassEnumizer.compiler.fir

import io.github.projectmapk.sealedClassEnumizer.compiler.EnumizeKey
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

// 自プラグインが生成した宣言か（origin の刻印による判定。docs/コンパイラプラグイン設計01.md §1）。
// IR 側の IrDeclaration.isGeneratedByEnumize と対になる。IC ラウンド外のファイル由来の宣言は
// 逆直列化で刻印を失うため、このラウンドで FIR が生成した宣言だけが true になる
val FirBasedSymbol<*>.isGeneratedByEnumize: Boolean
    get() = (origin as? FirDeclarationOrigin.Plugin)?.key == EnumizeKey
