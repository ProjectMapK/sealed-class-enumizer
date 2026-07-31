package io.github.projectmapk.sealedClassEnumizer

/**
 * Hierarchy-wide operations over kinds — the enum-like static side (`entries` / `valueOf`),
 * implemented by the companion object of each generated `Enumish`.
 *
 * Declared covariantly in [T] so companions of different hierarchies can be handled together, e.g.
 * as `List<EnumishCompanion<Enumish>>`, without projections.
 */
interface EnumishCompanion<out T : Enumish> {
    /**
     * All kinds of the hierarchy.
     *
     * Built lazily on first access; afterwards the same list instance is returned. The order is the
     * compiler-provided inheritor order (FQN-based) — NOT declaration order — and it changes when
     * leaves or their enclosing classes are renamed, added or removed. Do not persist positions in
     * this list; persist [Enumish.label] or a custom property instead.
     */
    val entries: List<T>

    /**
     * Returns the kind whose [Enumish.label] equals [value].
     *
     * @throws IllegalArgumentException if no kind has that label.
     */
    fun valueOf(value: String): T

    /**
     * Returns the kind whose [Enumish.label] equals [value], or `null` (an addition over enums).
     */
    fun valueOfOrNull(value: String): T?
}
