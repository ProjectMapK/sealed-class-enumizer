package org.wrongwrong.javaconsumer;

import org.junit.jupiter.api.Test;
import org.wrongwrong.fixtures.Command;
import org.wrongwrong.sealedClassEnumizer.EnumizedLabelKt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

// enum class 末端（V4）の Java 観測: enum 定数側 API（name()）と kind 側 API（getLabel()）が
// Java からも管轄別に併存する（docs/概要.md §1・§4、docs/テストケース管理.md TC-XM-057）
class JavaEnumLeafTest {
    // TC-XM-057: 定数の name() と kind（= 生成 companion）の getLabel() の併存・entries の 2 kind
    @Test
    void enumConstantNameAndKindLabelCoexist() {
        assertEquals("HELP", Command.Builtin.HELP.name());
        assertEquals("Builtin", Command.Builtin.Companion.getLabel());
        assertEquals("Builtin", EnumizedLabelKt.getLabel(Command.Builtin.HELP));
        assertEquals(
                List.of("Builtin", "Custom"),
                Command.Enumish.Companion.getEntries().stream().map(Command.Enumish::getLabel).toList());
    }

    // TC-XM-057: enum 定数からの asEnumish() は kind（companion のシングルトン）を返し、
    // valueOf("Builtin") とも同一
    @Test
    void enumConstantsShareTheSingleKind() {
        assertSame(Command.Builtin.Companion, Command.Builtin.HELP.asEnumish());
        assertSame(Command.Builtin.HELP.asEnumish(), Command.Builtin.VERSION.asEnumish());
        assertSame(Command.Builtin.Companion, Command.Enumish.Companion.valueOf("Builtin"));
    }
}
