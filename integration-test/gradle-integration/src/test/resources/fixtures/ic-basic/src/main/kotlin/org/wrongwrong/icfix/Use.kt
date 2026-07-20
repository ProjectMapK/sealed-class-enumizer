package org.wrongwrong.icfix

// else 無しの kind 単位 when（docs/概要.md §1）。編集ケース毎の網羅性再検査の観測点
fun describe(value: SI): String = when (value.asEnumish()) {
    Foo.Companion -> "foo"
    Bar -> "bar"
    Outer.Leaf -> "leaf"
}
