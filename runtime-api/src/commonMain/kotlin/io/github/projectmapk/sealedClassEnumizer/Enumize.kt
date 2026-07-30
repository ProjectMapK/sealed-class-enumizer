package io.github.projectmapk.sealedClassEnumizer

// Retention が BINARY なのは「ランタイム reflection に依存しない」ことの明示（RUNTIME にしない）。
// SOURCE にしないのは、ビルドツールや将来の keep ルールとの連携のため（docs/概要.md §2）。
// labelCase は階層の label のケース指定（docs/概要.md §4）。既定の PROJECT_DEFAULT はプロジェクト既定の
// 読み込みを指し、引数未指定は PROJECT_DEFAULT 指定と同義に扱われる
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Enumize(val labelCase: LabelCase = LabelCase.PROJECT_DEFAULT)
