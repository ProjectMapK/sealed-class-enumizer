package io.github.projectmapk.sealedClassEnumizer

import kotlin.reflect.KClass

/**
 * Base class of the per-hierarchy `EntriesHolder` the plugin generates: holds the entries list, its
 * lazy initialization and the label lookup. User code normally goes through the generated `Enumish`
 * companion ([EnumishCompanion]) instead of this type.
 *
 * Takes no constructor parameters so the generated subclass needs only a no-arg super call; the
 * hierarchy is identified by [enumizedRootClass].
 *
 * Do not read [entries] or the lookup functions from initializers of kinds (leaf objects or
 * companions): that re-enters the lazy initialization and the behavior is undefined — double
 * initialization, deadlock or a runtime failure depending on the platform. This is the same rule as
 * not touching `values()` from an enum constructor.
 */
abstract class EnumishEntriesHolder<T : Enumish> {
    /** Class literal of the hierarchy root (the `@Enumize` base); also used in failure messages. */
    protected abstract val enumizedRootClass: KClass<out Enumized<T>>

    /** Creates the entries in compiler-provided inheritor order; called once on first access. */
    protected abstract fun createEntries(): List<T>

    /** All kinds of the hierarchy; built once on first access, then reused. */
    val entries: List<T> by lazy { createEntries() }

    /**
     * Returns the first kind whose label equals [label], or `null`. Labels are unique per hierarchy
     * (enforced at compile time), so the match is unambiguous.
     */
    fun getByLabelOrNull(label: String): T? = entries.firstOrNull { it.label == label }

    /**
     * Returns the kind whose label equals [label]. The failure message identifies the hierarchy by
     * the root's simple name, because qualified names are unavailable on some platforms (e.g. JS).
     *
     * @throws IllegalArgumentException if no kind has that label.
     */
    fun getByLabel(label: String): T =
        getByLabelOrNull(label)
            ?: throw IllegalArgumentException(
                "No enumish entry with label '$label' in ${enumizedRootClass.simpleName}"
            )
}
