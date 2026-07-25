package org.wrongwrong.mpp.fixtures

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// enum 末端の label と Enum.name の併存が全ターゲットで一致することの box テスト
// （docs/テストケース管理.md TC-MPP-044・V4・docs/概要.md §1）
class CommandTest {
    // enum は全体で 1 kind（定数毎には展開しない）
    @Test
    fun enumLeafIsSingleKind() {
        assertEquals(listOf("Builtin", "Custom"), Command.Enumish.entries.map { it.label })
    }

    // HELP.name == "HELP"（enum 本来）・HELP.label == "Builtin"（kind）が管轄分離で併存する
    @Test
    fun enumNameAndKindLabelCoexist() {
        val help: Command = Command.Builtin.HELP
        assertEquals(listOf("HELP", "Builtin"), listOf(Command.Builtin.HELP.name, help.label))
    }

    // 定数がどれでも kind は同一（enum の companion が kind）。
    // enum 型受け手からの asEnumish()（返り値型 = 生成 companion）で直接観測する
    @Test
    fun allConstantsShareTheKind() {
        assertSame(Command.Builtin.HELP.asEnumish(), Command.Builtin.VERSION.asEnumish())
        assertSame(Command.Builtin.Companion, Command.Builtin.HELP.asEnumish())
    }

    // 定数側の表示（Enum.toString = name）と kind 側の表示（toString = label）は別物
    @Test
    fun constantAndKindHaveSeparateToString() {
        val help: Command = Command.Builtin.HELP
        assertEquals(
            listOf("HELP", "Builtin"),
            listOf(Command.Builtin.HELP.toString(), help.asEnumish().toString()),
        )
    }
}
