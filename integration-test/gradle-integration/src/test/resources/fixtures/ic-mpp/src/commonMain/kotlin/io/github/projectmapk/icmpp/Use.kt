package io.github.projectmapk.icmpp

// else 無しの kind-when（各ラウンドで診断が偽陽性を出さないことの観測点）
fun describe(value: SI): String = when (value.asEnumish()) {
    LeafA.Companion -> "a"
    LeafB -> "b"
}
