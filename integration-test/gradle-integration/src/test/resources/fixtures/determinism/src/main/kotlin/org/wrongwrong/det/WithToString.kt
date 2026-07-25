package org.wrongwrong.det

// 階層外の親クラス（継承経路上の Any 以外の具象 toString = 原則 1(b) の材料）
abstract class WithToString {
    override fun toString(): String = "parent"
}
