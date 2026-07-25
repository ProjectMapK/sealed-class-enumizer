package org.wrongwrong.fixtures.valueclass

import org.wrongwrong.sealedClassEnumizer.Enumize

// value class 末端の boxing 挙動フィクスチャ（docs/エッジケースへの対応方針.md テスト項目のメモ。末端は Wrapped.kt）
@Enumize sealed interface Valued
