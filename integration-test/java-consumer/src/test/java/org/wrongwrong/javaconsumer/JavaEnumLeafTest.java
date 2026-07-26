package org.wrongwrong.javaconsumer;

import org.junit.jupiter.api.Test;
import org.wrongwrong.fixtures.enumleaf.Command;
import org.wrongwrong.sealedClassEnumizer.EnumizedLabelKt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

// enum class 末端（V4）の Java 観測: enum 定数側 API（name()）と kind 側 API（getLabel()）が
// Java からも管轄別に併存する（docs/test/ケース05-境界横断.md XMP-30）
class JavaEnumLeafTest {
    // docs/test/ケース05-境界横断.md XMP-30: 定数の name() と kind（= 生成 companion）の getLabel() の併存。
    // entries は 4 kind（enum 2 + data object + data class）で、"HELP" は定数名でなく同名 data object の label
    @Test
    void enumConstantNameAndKindLabelCoexist() {
        assertEquals("HELP", Command.Builtin.HELP.name());
        assertEquals("Builtin", Command.Builtin.Companion.getLabel());
        assertEquals("Builtin", EnumizedLabelKt.getLabel(Command.Builtin.HELP));
        assertEquals("HELP", Command.HELP.INSTANCE.getLabel());
        assertEquals(
                List.of("Builtin", "Custom", "HELP", "Verb"),
                Command.Enumish.Companion.getEntries().stream().map(Command.Enumish::getLabel).toList());
    }

    // docs/test/ケース05-境界横断.md XMP-30: 全定数の asEnumish() は kind（companion のシングルトン）を
    // 返し valueOf("Builtin") とも同一。明示 companion の Verb も同形で kind に流用される
    @Test
    void enumConstantsShareTheSingleKind() {
        assertSame(Command.Builtin.Companion, Command.Builtin.HELP.asEnumish());
        assertSame(Command.Builtin.HELP.asEnumish(), Command.Builtin.VERSION.asEnumish());
        assertSame(Command.Builtin.Companion, Command.Enumish.Companion.valueOf("Builtin"));
        assertSame(Command.Verb.Companion, Command.Verb.GET.asEnumish());
        assertSame(Command.Verb.GET.asEnumish(), Command.Verb.POST.asEnumish());
    }
}
