package io.github.projectmapk.mpp.fixtures.order

// 小文字始まりのトップレベル末端（docs/概要.md §5 の実測形）。大文字小文字を区別する序数比較では
// 'a'(97) > 'Z'(90) のため末尾になる（大小無視の辞書順なら先頭になる）
@Suppress("ClassName") data object aLower : FqnOrder
