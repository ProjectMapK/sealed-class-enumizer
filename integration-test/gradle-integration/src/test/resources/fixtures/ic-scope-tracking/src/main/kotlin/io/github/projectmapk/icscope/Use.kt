package io.github.projectmapk.icscope

// kind-when の観測点。ScAliasLeaf は名指ししない（階層離脱ラウンドでも成立する形 = else 受け）
fun describe(value: ScBase): String = when (value.asEnumish()) {
    ScA -> "a"
    else -> "other"
}
