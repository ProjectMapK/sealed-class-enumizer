package org.wrongwrong.fixtures.enumleaf

import org.wrongwrong.sealedClassEnumizer.Enumize

// enum 末端深掘りの合成階層（docs/test/ケース01-生成と実行時API.md §3。V4）。
// enum は全体で 1 kind（定数毎には展開しない）。Builtin = 自動生成 companion・
// Verb = 明示 companion + 定数 toString override・HELP = enum 定数と同名の data object（label 領域の対照）
@Enumize
sealed interface Command {
    enum class Builtin : Command {
        HELP,
        VERSION,
    }

    enum class Verb : Command {
        GET {
            override fun toString(): String = "get!"
        },
        POST;

        companion object
    }

    // enum 定数 Builtin.HELP と同名の末端。定数名は label 領域外のため衝突しない（API-20）
    data object HELP : Command

    data class Custom(val raw: String) : Command
}
