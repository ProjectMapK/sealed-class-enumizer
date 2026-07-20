package org.wrongwrong.kt86121

// else 無しの kind-when（各ラウンドで診断が偽陽性を出さないことの観測点 = TC-IC-059）
fun describe(value: SI): String = when (value.asEnumish()) {
    LeafA.Companion -> "a"
    LeafB -> "b"
    LeafC.Companion -> "c"
}
