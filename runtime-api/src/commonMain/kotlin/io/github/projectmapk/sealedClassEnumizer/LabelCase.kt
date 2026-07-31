package io.github.projectmapk.sealedClassEnumizer

/**
 * Case conversion applied to leaf simple names when deriving labels ([Enumize.labelCase]).
 *
 * Word splitting matches kotlinx.serialization's `JsonNamingStrategy`: a run of upper-case letters
 * is kept as one word, and only when followed by a lower-case letter does its last letter start the
 * next word (`HTTPServer` → `HTTP_SERVER`). The behavior is locale-independent.
 *
 * Labels are meant to be persisted, so conversion results are frozen and will not change in future
 * releases. Labels given via [EnumishLabel] are exempt from conversion.
 */
enum class LabelCase {
    /**
     * Reads the project-wide default configured in the Gradle DSL, falling back to [AS_DECLARED].
     * Omitting the [Enumize.labelCase] argument means the same as specifying this.
     */
    PROJECT_DEFAULT,

    /** No conversion: the label is the declared simple name. */
    AS_DECLARED,

    /** `FooBar` → `FOO_BAR`. */
    UPPER_SNAKE_CASE,

    /** `FooBar` → `foo_bar`. */
    SNAKE_CASE,

    /** `FooBar` → `foo-bar`. */
    KEBAB_CASE,
}
