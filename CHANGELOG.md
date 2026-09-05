# Changelog

Notable changes to this project are documented here.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), with an additional
`Internal` section for changes that leave the published API and behavior untouched.

Artifacts are versioned as `<KotlinVersion>-<PluginVersion>`, and all artifacts are released together
under the same version. See [Versioning and support policy](README.md#versioning-and-support-policy)
for what each part means.

## [Unreleased]

### Fixed

- #63: `@Enumize(labelCase = ...)` naming the `LabelCase` entry through an import alias is now
  converted consistently. The compile-time uniqueness check previously fell back to the project
  default in that form, so labels that collide after conversion could pass compilation.
- #63: Leaves whose supertype is written as a simple name resolved through an enclosing class's
  companion object or superclass are now recognized as hierarchy members. Previously they received
  no generated API, failing with an "abstract member not implemented" error, or with an internal
  compiler error for abstract leaves.
- #63: `ENUMIZE_EXTENSION_SHADOWED` is no longer reported for a member function named `label`,
  which does not shadow the `label` extension property.

### Internal

- #64: Internal refactoring of the compiler plugin and the build-tool plugins; no behavioral change.

## [2.4.10-0.1.2]

### Fixed

- #56: A blank `@EnumishLabel` value now falls back to the label derived from the leaf name, instead
  of being used as the label. It is reachable only by suppressing the `ENUMIZE_INVALID_LABEL` error
  that a blank value triggers; the generated label then disagreed with the compile-time uniqueness
  check.

### Internal

- #56: Hierarchy lookups in the compiler plugin are cached, lowering the compilation overhead the
  plugin adds to modules with large class graphs.
- #56: Building `entries` now fails with a diagnostic message if a leaf is missing its kind, rather
  than omitting it. The invariant holds for every accepted hierarchy, so this only guards against a
  defect in the plugin itself.

## [2.4.10-0.1.1]

### Added

- #43: Runtime API artifacts for the `linuxArm64` and `androidNative*` (Arm32 / Arm64 / X64 / X86)
  targets.

## [2.4.10-0.1.0]

Initial release.
