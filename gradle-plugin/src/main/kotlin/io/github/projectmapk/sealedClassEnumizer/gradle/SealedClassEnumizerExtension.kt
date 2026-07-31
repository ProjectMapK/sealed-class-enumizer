package io.github.projectmapk.sealedClassEnumizer.gradle

import org.gradle.api.provider.Property

/**
 * Configuration for the sealed-class-enumizer Gradle plugin:
 * ```kotlin
 * sealedClassEnumizer {
 *     addRuntimeDependency = true
 *     labelCase = LabelCase.AS_DECLARED
 * }
 * ```
 */
abstract class SealedClassEnumizerExtension {
    /**
     * Whether to add the runtime API dependency to each compilation automatically: `api` scope for
     * production compilations (the generated API exposes runtime API types as supertypes, so
     * consumers need them on their compile classpath) and `implementation` for test compilations.
     * Defaults to `true`; disable it to declare the dependency manually.
     */
    abstract val addRuntimeDependency: Property<Boolean>

    // プロパティ名はコンパイラプラグインの labelCase オプション（= @Enumize の引数名）と同名に揃える。
    // convention はコンパイラプラグインの組み込み既定（EnumizeLabelCase.BUILT_IN_DEFAULT）と同値の
    // AS_DECLARED に固定し、二重既定の乖離を防ぐ
    /**
     * The project-wide default label case, applied to hierarchies whose `@Enumize` does not specify
     * a concrete `labelCase`. Defaults to [LabelCase.AS_DECLARED] (no conversion).
     */
    abstract val labelCase: Property<LabelCase>

    init {
        addRuntimeDependency.convention(true)
        labelCase.convention(LabelCase.AS_DECLARED)
    }
}
