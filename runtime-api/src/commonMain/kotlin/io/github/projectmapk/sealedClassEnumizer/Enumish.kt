package io.github.projectmapk.sealedClassEnumizer

import kotlin.reflect.KClass

// toString は宣言しない: interface での抽象再宣言は Any の実装では充足されず、
// 宣言しても充足の成立条件を増やすだけで利点が無い（docs/概要.md §2）
/**
 * Base type of the kinds generated for `@Enumize` hierarchies.
 *
 * For each hierarchy the plugin generates a nested `sealed interface Enumish` extending this
 * interface. Its values — one singleton per leaf, called kinds — play the role of enum constants:
 * the kind of a leaf `object` is the object itself, and the kind of a leaf `class` is its companion
 * object.
 */
interface Enumish {
    /**
     * The label of this kind — the counterpart of `Enum.name`.
     *
     * Defaults to the leaf's simple name, subject to [LabelCase] conversion and to the
     * [EnumishLabel] override. Unique within a hierarchy (enforced at compile time).
     */
    val label: String

    /**
     * The companion object of the generated `Enumish` type this kind belongs to — the
     * reflection-free way from a kind to the hierarchy-wide operations ([EnumishCompanion.entries]
     * / [EnumishCompanion.valueOf]); the counterpart of `Enum.getDeclaringClass`. Generated types
     * covariantly narrow this to their concrete companion type.
     */
    val enumishCompanion: EnumishCompanion<Enumish>

    /**
     * The class literal of the leaf this kind corresponds to — the bridge to reflection-based
     * libraries (kotlin-reflect, serialization, DI). For a non-final leaf this is the leaf class
     * itself, which may differ from a value's runtime class.
     */
    val enumizedClass: KClass<out Enumized<*>>
}
