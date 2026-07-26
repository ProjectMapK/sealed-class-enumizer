package org.wrongwrong.fixtures.enumleaf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.sealedClassEnumizer.label

// enum 末端の全観測（docs/test/ケース01-生成と実行時API.md §3。V4）
class EnumLeafTest {
    // docs/test/ケース01-生成と実行時API.md API-19: enum 末端は全体で 1 kind（定数非展開）。
    // 複数 enum は各 1 kind（自動 companion の Builtin・明示 companion の Verb）
    @Test
    fun eachEnumLeafIsExactlyOneKind() {
        assertEquals(
            listOf("Builtin", "Custom", "HELP", "Verb"),
            Command.Enumish.entries.map { it.label },
        )
        assertSame(Command.Builtin.HELP.asEnumish(), Command.Builtin.VERSION.asEnumish())
        assertSame(Command.Builtin.Companion, Command.Builtin.HELP.asEnumish())
        assertSame(Command.Verb.GET.asEnumish(), Command.Verb.POST.asEnumish())
    }

    // docs/test/ケース01-生成と実行時API.md API-20: Enum.name と label は併存し、enum 定数名は
    // label 領域外（valueOf("HELP") = 同名 data object・valueOf("Builtin") = enum kind）
    @Test
    fun nameAndLabelDomainsAreSeparate() {
        val help: Command = Command.Builtin.HELP
        assertEquals(listOf("HELP", "Builtin"), listOf(Command.Builtin.HELP.name, help.label))
        assertSame(Command.HELP, Command.Enumish.valueOf("HELP"))
        assertSame(Command.Builtin.Companion, Command.Enumish.valueOf("Builtin"))
    }

    // docs/test/ケース01-生成と実行時API.md API-21: kind の toString（2 原則で生成）と
    // 定数側 toString override は管轄の異なる別物で相互無影響
    @Test
    fun kindAndConstantToStringAreIndependent() {
        assertEquals(
            listOf("get!", "POST", "Verb"),
            listOf(
                Command.Verb.GET.toString(),
                Command.Verb.POST.toString(),
                Command.Verb.Companion.toString(),
            ),
        )
        assertEquals(
            listOf("HELP", "Builtin"),
            listOf(Command.Builtin.HELP.toString(), Command.Builtin.Companion.toString()),
        )
    }

    // docs/test/ケース01-生成と実行時API.md API-22: 明示 companion は kind として流用され、
    // Verb.entries / Command.Enumish.entries / kind の 3 名前空間が併存する
    @Test
    fun explicitCompanionIsReusedAsKind() {
        assertSame(Command.Verb.Companion, Command.Verb.GET.asEnumish())
        assertSame(Command.Verb.Companion, Command.Enumish.valueOf("Verb"))
        assertEquals(Command.Verb::class, Command.Verb.Companion.enumizedClass)
        // enum 自前の entries（定数列）と生成 entries（kind 列）は独立の名前空間
        assertEquals(listOf(Command.Verb.GET, Command.Verb.POST), Command.Verb.entries.toList())
        assertEquals(4, Command.Enumish.entries.size)
    }

    // docs/test/ケース03-順序.md ORD-04: enum 末端は 1 kind として自 FQN 位置に置かれ、
    // 定数名（GET / POST / VERSION）は entries に現れない
    @Test
    fun enumLeafSortsWithoutConstantExpansion() {
        val labels = Command.Enumish.entries.map { it.label }
        assertEquals(listOf("Builtin", "Custom", "HELP", "Verb"), labels)
        // "HELP" は定数側でなく同名 data object 末端の label である
        assertSame(Command.HELP, Command.Enumish.valueOf("HELP"))
    }
}
