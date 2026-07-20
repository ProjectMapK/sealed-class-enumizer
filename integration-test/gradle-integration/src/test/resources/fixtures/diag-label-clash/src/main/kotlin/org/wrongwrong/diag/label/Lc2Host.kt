package org.wrongwrong.diag.label

// TC-DIAG-040: companion 自身が末端の場合に限りその宣言名（Foo2）が label になる
class Lc2Host {
    companion object Foo2 : Lc2Si
}
