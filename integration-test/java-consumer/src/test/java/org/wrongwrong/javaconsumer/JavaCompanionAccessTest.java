package org.wrongwrong.javaconsumer;

import kotlin.jvm.JvmClassMappingKt;
import org.junit.jupiter.api.Test;
import org.wrongwrong.fixtures.SI;
import org.wrongwrong.sealedClassEnumizer.EnumizedLabelKt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Java 消費側: @JvmStatic 非付与（v1）のため、interface 上に公開される static フィールド Companion を
// 経由して生成 API を呼ぶ（docs/概要.md §3「Java からの利用」・docs/テストケース管理.md TC-XM-026 /
// TC-XM-029 / TC-BOX-054）。SI.Enumish.getEntries() 形の static アクセスが不可なこと（TC-XM-027）は
// コンパイル失敗を要するため gradle-integration 担当
class JavaCompanionAccessTest {
    // TC-XM-026 / TC-BOX-054: Companion フィールド経由の getEntries / valueOf / valueOfOrNull
    //（entries はプロパティなので Java では getEntries() になる）
    @Test
    void entriesAndValueOfResolveViaCompanionField() {
        List<SI.Enumish> entries = SI.Enumish.Companion.getEntries();
        assertEquals(List.of("Bar", "Foo"), entries.stream().map(SI.Enumish::getLabel).toList());
        assertSame(SI.Foo.Companion, SI.Enumish.Companion.valueOf("Foo"));
        assertSame(SI.Bar.INSTANCE, SI.Enumish.Companion.valueOfOrNull("Bar"));
        assertNull(SI.Enumish.Companion.valueOfOrNull("X"));
    }

    // TC-XM-026: valueOf 失敗時の IllegalArgumentException 文言の Java からの観測
    @Test
    void valueOfFailureMessageIsObservableFromJava() {
        IllegalArgumentException failure =
                assertThrows(IllegalArgumentException.class, () -> SI.Enumish.Companion.valueOf("X"));
        assertEquals("No enumish entry with label 'X' in SI", failure.getMessage());
    }

    // TC-XM-029: kind のメンバー（getLabel / getEnumizedClass / getEnumishCompanion）と値側の
    // asEnumish() / label 拡張（Java からはトップレベル static の EnumizedLabelKt.getLabel）。
    // 共変 override の bridge メソッドが Java から正しく解決される
    @Test
    void kindMembersResolveViaBridges() {
        assertEquals("Foo", SI.Foo.Companion.getLabel());
        assertEquals(SI.Foo.class, JvmClassMappingKt.getJavaClass(SI.Foo.Companion.getEnumizedClass()));
        assertSame(SI.Enumish.Companion, SI.Foo.Companion.getEnumishCompanion());
        SI si = new SI.Foo(7);
        assertSame(SI.Foo.Companion, si.asEnumish());
        assertEquals("Foo", EnumizedLabelKt.getLabel(si));
    }
}
