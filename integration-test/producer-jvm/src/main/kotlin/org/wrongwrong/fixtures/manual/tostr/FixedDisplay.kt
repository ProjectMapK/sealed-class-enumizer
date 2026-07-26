package org.wrongwrong.fixtures.manual.tostr

// final override の toString を持つ親クラス（docs/test/ケース01-生成と実行時API.md API-35。
// 生成をスキップするため final 衝突は起きない）
open class FixedDisplay {
    final override fun toString(): String = "fixed-display"
}
