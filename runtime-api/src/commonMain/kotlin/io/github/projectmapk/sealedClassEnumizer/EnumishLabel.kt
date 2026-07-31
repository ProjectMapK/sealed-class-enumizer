package io.github.projectmapk.sealedClassEnumizer

/**
 * Overrides the label of the annotated leaf declaration.
 *
 * [value] becomes the final label as-is; no [LabelCase] conversion is applied. Typical uses are
 * resolving a label clash between leaves, and keeping persisted labels stable when a leaf is
 * renamed.
 *
 * Checked at compile time: only leaf declarations (the origin of a label) may carry this
 * annotation, and blank values are rejected.
 *
 * The retention is [AnnotationRetention.BINARY] for the same reason as [Enumize].
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class EnumishLabel(val value: String)
