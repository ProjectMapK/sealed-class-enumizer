package org.wrongwrong.icrounds

// else 無しの kind-when（各ラウンドで診断が偽陽性を出さないことの観測点 = docs/test/ケース06-ビルド動態.md BLD-29）
fun describe(value: SI): String = when (value.asEnumish()) {
    LeafA.Companion -> "a"
    LeafB -> "b"
    LeafC.Companion -> "c"
}
