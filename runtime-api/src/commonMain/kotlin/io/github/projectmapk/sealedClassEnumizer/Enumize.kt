package io.github.projectmapk.sealedClassEnumizer

// Retention が BINARY である理由は docs/概要.md §2。labelCase は階層の label のケース指定（docs/概要.md §4）
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Enumize(val labelCase: LabelCase = LabelCase.PROJECT_DEFAULT)
