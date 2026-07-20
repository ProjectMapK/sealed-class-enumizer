package org.wrongwrong.sealedClassEnumizer

// 値から kind の label を引く短縮。メンバーは拡張より常に優先されるため、階層のメンバーが
// label を持つ場合は黙って隠される — 確実な取得は asEnumish().label（docs/概要.md §2 の注意）
val <T : Enumish> Enumized<T>.label: String get() = asEnumish().label
