package org.wrongwrong.fixtures.nested

import org.wrongwrong.sealedClassEnumizer.Enumize

// 中間 sealed の入れ子展開フィクスチャ（TC-BOX-006・docs/コンパイラプラグイン設計00.md §6.2 の実測形）。
// 継承者はファイル分散で定義する: Mid.kt / Aaa.kt / Bbb.kt
@Enumize sealed interface NestedRoot
