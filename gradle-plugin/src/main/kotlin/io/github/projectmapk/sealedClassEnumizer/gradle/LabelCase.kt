package io.github.projectmapk.sealedClassEnumizer.gradle

// runtime-api の LabelCase から PROJECT_DEFAULT を除いた具体ケースと 1:1 対応する。
// エントリ名の一致は LabelCaseTest がガードし、値はコンパイラプラグインの labelCase オプションへ
// エントリ名のまま渡される
/**
 * Label cases selectable as the project-wide default ([SealedClassEnumizerExtension.labelCase]).
 *
 * Mirrors the runtime API's `LabelCase` without `PROJECT_DEFAULT` — the project default is what
 * this DSL defines, so a "read the project default" entry would be self-referential here. The
 * conversion rules are documented on the runtime API's `LabelCase` and are frozen across releases.
 */
enum class LabelCase {
    /** No conversion: the label is the declared simple name. */
    AS_DECLARED,

    /** `FooBar` → `FOO_BAR`. */
    UPPER_SNAKE_CASE,

    /** `FooBar` → `foo_bar`. */
    SNAKE_CASE,

    /** `FooBar` → `foo-bar`. */
    KEBAB_CASE,
}
