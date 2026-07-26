package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-62 用: toString の抽象再宣言（Any の実装では充足されない）
abstract class AtAbs {
    abstract override fun toString(): String
}
