package org.wrongwrong.sweep.xn

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-VIS-004 / TC-XM-051 用: internal 一色の階層（基底も末端も実効 internal）。
// producer 内では全機能が通常どおり使える（基底の可視性は制限ではない）
@Enumize
internal sealed interface SwXnInt {
    data object Bar : SwXnInt

    data class Foo(val v: Int) : SwXnInt
}

// 同一モジュール内側からの利用の対照（コンパイル成立自体が検証）
internal fun swXnIntInside(): Int = SwXnInt.Enumish.entries.size
