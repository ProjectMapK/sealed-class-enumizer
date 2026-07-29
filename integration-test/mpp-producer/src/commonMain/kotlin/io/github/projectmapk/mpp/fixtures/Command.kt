package io.github.projectmapk.mpp.fixtures

import io.github.projectmapk.sealedClassEnumizer.Enumize

// enum class が sealed interface を継承しているケースの MPP 版（V4。docs/概要.md §1・
// docs/test/ケース05-境界横断.md XMP-34）。enum は全体で 1 kind（定数毎には展開しない）
@Enumize
sealed interface Command {
    data class Custom(val raw: String) : Command

    enum class Builtin : Command {
        HELP,
        VERSION,
    }
}
