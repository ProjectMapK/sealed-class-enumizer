package io.github.projectmapk.sealedClassEnumizer

/**
 * Instructs the sealed-class-enumizer compiler plugin to generate enum-like operations for the
 * annotated sealed class or sealed interface.
 *
 * ```kotlin
 * @Enumize
 * sealed interface SI {
 *     data class Foo(val v: Int) : SI
 *     data object Bar : SI
 * }
 * ```
 *
 * generates a nested `sealed interface SI.Enumish` implementing [Enumish], whose values ("kinds")
 * are singletons representing the leaves of the hierarchy:
 * ```kotlin
 * SI.Enumish.entries          // [Bar, Foo] — all kinds (see [EnumishCompanion.entries])
 * SI.Enumish.valueOf("Foo")   // label-based lookup (see [EnumishCompanion.valueOf])
 * val si: SI = SI.Foo(42)
 * si.asEnumish()              // the kind of a value (see [Enumized.asEnumish])
 * si.label                    // "Foo" — the `label` extension, enum's `name` counterpart
 * ```
 *
 * Everything is generated at compile time; no runtime reflection is involved and all Kotlin
 * Multiplatform targets are supported.
 *
 * [labelCase] chooses the case conversion applied to leaf simple names when deriving labels. The
 * default [LabelCase.PROJECT_DEFAULT] reads the project-wide default configured in the Gradle DSL
 * (falling back to no conversion), and omitting the argument means the same. Individual leaves can
 * override their label with [EnumishLabel].
 *
 * The retention is [AnnotationRetention.BINARY]: the annotation is never read at runtime — the
 * explicit statement that nothing here depends on runtime reflection — while staying visible in
 * metadata to build tooling.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Enumize(val labelCase: LabelCase = LabelCase.PROJECT_DEFAULT)
