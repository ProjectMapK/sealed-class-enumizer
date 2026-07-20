package org.wrongwrong.fixtures

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// enum class 末端（V4）の box テスト（docs/概要.md §1「enum が sealed interface を継承しているケース」）
class CommandTest {
    // enum は全体で 1 kind（定数毎には展開しない）
    @Test
    fun enumLeafIsSingleKind() {
        assertEquals(listOf("Builtin", "Custom"), Command.Enumish.entries.map { it.label })
    }

    // Enum.name（final メンバー）と kind の label は併存する
    @Test
    fun enumNameAndKindLabelCoexist() {
        val help: Command = Command.Builtin.HELP
        assertEquals(listOf("HELP", "Builtin"), listOf(Command.Builtin.HELP.name, help.label))
    }

    // 定数がどれでも kind は同一（enum の companion が kind）
    @Test
    fun allConstantsShareTheKind() {
        assertSame(Command.Builtin.HELP.asEnumish(), Command.Builtin.VERSION.asEnumish())
        assertSame(Command.Enumish.valueOf("Builtin"), Command.Builtin.HELP.asEnumish())
    }

    // TC-BOX-051: enum 末端の enumizedClass は enum class 自身を指し、valueOf は companion（kind）を返す
    @Test
    fun enumLeafEnumizedClassAndValueOf() {
        assertEquals(Command.Builtin::class, Command.Builtin.HELP.asEnumish().enumizedClass)
        assertSame(Command.Builtin.HELP.asEnumish(), Command.Enumish.valueOf("Builtin"))
    }

    // 定数側の表示（Enum.toString = name）と kind 側の表示（toString = label）は管轄の異なる別物
    @Test
    fun constantAndKindHaveSeparateToString() {
        assertEquals(
            listOf("HELP", "Builtin"),
            listOf(Command.Builtin.HELP.toString(), Command.Builtin.HELP.asEnumish().toString()),
        )
    }
}
