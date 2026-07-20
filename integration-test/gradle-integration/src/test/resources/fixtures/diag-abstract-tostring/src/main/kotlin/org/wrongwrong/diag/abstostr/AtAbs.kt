package org.wrongwrong.diag.abstostr

// TC-DIAG-084 用: toString の抽象再宣言（Any の実装では充足されない）
abstract class AtAbs {
    abstract override fun toString(): String
}
