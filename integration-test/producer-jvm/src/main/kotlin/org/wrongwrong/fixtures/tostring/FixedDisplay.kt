package org.wrongwrong.fixtures.tostring

// final override の toString を持つ親クラス（TC-BOX-046。生成をスキップするため final 衝突は起きない）
open class FixedDisplay {
    final override fun toString(): String = "fixed-display"
}
