package org.wrongwrong.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize

// enum class が sealed interface を継承しているケース（V4。docs/概要.md §1）。
// enum は全体で 1 kind（定数毎には展開しない）
@Enumize
sealed interface Command {
    data class Custom(val raw: String) : Command

    enum class Builtin : Command {
        HELP,
        VERSION,
    }
}
