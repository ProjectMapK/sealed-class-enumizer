package org.wrongwrong.sealedClassEnumizer

// Retention が BINARY なのは「ランタイム reflection に依存しない」ことの明示（RUNTIME にしない）。
// SOURCE にしないのは、ビルドツールや将来の keep ルールとの連携のため（docs/概要.md §2）。
@Target(AnnotationTarget.CLASS) @Retention(AnnotationRetention.BINARY) annotation class Enumize
