package org.wrongwrong.javaconsumer;

import org.junit.jupiter.api.Test;
import org.wrongwrong.fixtures.Command;
import org.wrongwrong.fixtures.SI;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Java 21 パターンマッチング switch（JEP 441）: producer（jvmTarget 17）が出力する sealed の
// PermittedSubclasses 属性により、default 無しの switch が網羅と判定される（docs/概要.md §3
// 「Java 側の網羅 switch」・docs/テストケース管理.md TC-XM-028 / TC-BOX-055）。
// 古典的な enum switch(si) が使えない負のケースはコンパイル失敗を要するため gradle-integration 担当
class JavaPatternSwitchTest {
    // TC-XM-028 / TC-BOX-055: 値単位 SI に対するパターン switch が default 無しで網羅する
    @Test
    void patternSwitchOverSealedValueIsExhaustive() {
        List<SI> values = List.of(new SI.Foo(1), SI.Bar.INSTANCE);
        List<String> results = values.stream().map(si -> switch (si) {
            case SI.Foo foo -> "foo:" + foo.getV();
            case SI.Bar bar -> "bar";
        }).toList();
        assertEquals(List.of("foo:1", "bar"), results);
    }

    // TC-XM-028: enum class 末端を含む階層（Command）でも、末端型パターン（enum 型を含む）で
    // default 無しの網羅が成立する
    @Test
    void patternSwitchCoversEnumLeafHierarchy() {
        List<Command> commands = List.of(new Command.Custom("x"), Command.Builtin.HELP);
        List<String> results = commands.stream().map(command -> switch (command) {
            case Command.Custom custom -> "custom:" + custom.getRaw();
            case Command.Builtin builtin -> "builtin:" + builtin.name();
        }).toList();
        assertEquals(List.of("custom:x", "builtin:HELP"), results);
    }
}
