package org.wrongwrong.javaconsumer;

import org.junit.jupiter.api.Test;
import org.wrongwrong.fixtures.enumleaf.Command;
import org.wrongwrong.fixtures.si.SI;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Java 21 パターンマッチング switch（JEP 441）: producer（jvmTarget 17）が出力する sealed の
// PermittedSubclasses 属性により、default 無しの switch が網羅と判定される
// （docs/test/ケース05-境界横断.md XMP-29）。
// NG 固定（docs/test/ケース05-境界横断.md XMP-29）: 生成 kind（SI.Foo.Companion 等）は Java の enum
// 定数ではないため、古典 enum switch は構成できない:
//
//   SI.Enumish kind = si.asEnumish();
//   switch (kind) { case Foo: ... }   // kind は enum 型でなく case ラベルにできない
class JavaPatternSwitchTest {
    // docs/test/ケース05-境界横断.md XMP-29: 値単位 SI に対するパターン switch が default 無しで網羅する
    @Test
    void patternSwitchOverSealedValueIsExhaustive() {
        List<SI> values = List.of(new SI.Foo(1), SI.Bar.INSTANCE);
        List<String> results = values.stream().map(si -> switch (si) {
            case SI.Foo foo -> "foo:" + foo.getV();
            case SI.Bar bar -> "bar";
        }).toList();
        assertEquals(List.of("foo:1", "bar"), results);
    }

    // docs/test/ケース05-境界横断.md XMP-29: enum 末端 2 つ + data object + data class の Command 階層でも、
    // 末端型パターン（enum 型を含む）で default 無しの網羅が成立する
    @Test
    void patternSwitchCoversEnumLeafHierarchy() {
        List<Command> commands =
                List.of(new Command.Custom("x"), Command.Builtin.HELP, Command.Verb.GET, Command.HELP.INSTANCE);
        List<String> results = commands.stream().map(command -> switch (command) {
            case Command.Custom custom -> "custom:" + custom.getRaw();
            case Command.Builtin builtin -> "builtin:" + builtin.name();
            case Command.Verb verb -> "verb:" + verb.name();
            case Command.HELP help -> "help";
        }).toList();
        assertEquals(List.of("custom:x", "builtin:HELP", "verb:GET", "help"), results);
    }
}
