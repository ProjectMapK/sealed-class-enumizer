package io.github.projectmapk.sealedClassEnumizer

// 末端の label の明示指定（docs/概要.md §4・docs/エッジケースへの対応方針.md §3）。
// ケース変換（LabelCase）は適用されず、この値がそのまま最終 label になる。
// 付与できるのは label の由来となる末端宣言のみで、空白のみの値は不可（ENUMIZE_INVALID_LABEL）。
// Retention が BINARY である理由は Enumize と同じ（docs/概要.md §2）
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class EnumishLabel(val value: String)
