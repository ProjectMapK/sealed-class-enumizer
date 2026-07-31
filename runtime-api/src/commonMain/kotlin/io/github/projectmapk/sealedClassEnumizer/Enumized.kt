package io.github.projectmapk.sealedClassEnumizer

/** Supertype injected into every `@Enumize` hierarchy root, connecting values to their kinds. */
interface Enumized<out T : Enumish> {
    /**
     * Returns the kind of this value — the singleton representing the leaf this value belongs to.
     * Values of subtypes of a non-final leaf are absorbed into that leaf's kind.
     *
     * Unlike the `label` extension, this member cannot be shadowed by user declarations, so it is
     * the reliable path to kind data.
     */
    fun asEnumish(): T
}
