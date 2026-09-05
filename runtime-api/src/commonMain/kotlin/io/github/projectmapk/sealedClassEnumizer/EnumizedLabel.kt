package io.github.projectmapk.sealedClassEnumizer

/**
 * Shortcut for `asEnumish().label` — the counterpart of `Enum.name` on the value side.
 *
 * Member properties always take precedence over extension properties: when the hierarchy declares
 * or inherits its own `label` property, this extension is silently shadowed and `x.label` returns
 * the user property (the compiler plugin warns about such shapes). Use `asEnumish().label` to
 * obtain the kind's label reliably.
 */
val <T : Enumish> Enumized<T>.label: String
    get() = asEnumish().label
