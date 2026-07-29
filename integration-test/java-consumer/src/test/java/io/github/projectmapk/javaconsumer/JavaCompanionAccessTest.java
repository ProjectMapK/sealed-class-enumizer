package io.github.projectmapk.javaconsumer;

import kotlin.jvm.JvmClassMappingKt;
import org.junit.jupiter.api.Test;
import io.github.projectmapk.fixtures.si.SI;
import io.github.projectmapk.sealedClassEnumizer.EnumizedLabelKt;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Java 消費側: @JvmStatic 非付与（v1）のため、interface 上に公開される static フィールド Companion を
// 経由して生成 API を呼ぶ（docs/test/ケース05-境界横断.md XMP-27 / XMP-28）
class JavaCompanionAccessTest {
    // docs/test/ケース05-境界横断.md XMP-27: Companion フィールド経由の getEntries / valueOf / valueOfOrNull
    //（entries はプロパティなので Java では getEntries() になる）
    @Test
    void entriesAndValueOfResolveViaCompanionField() {
        List<SI.Enumish> entries = SI.Enumish.Companion.getEntries();
        assertEquals(List.of("Bar", "Foo"), entries.stream().map(SI.Enumish::getLabel).toList());
        assertSame(SI.Foo.Companion, SI.Enumish.Companion.valueOf("Foo"));
        assertSame(SI.Bar.INSTANCE, SI.Enumish.Companion.valueOfOrNull("Bar"));
        assertNull(SI.Enumish.Companion.valueOfOrNull("X"));
    }

    // docs/test/ケース05-境界横断.md XMP-27: valueOf 失敗時の IllegalArgumentException 文言の Java 観測
    @Test
    void valueOfFailureMessageIsObservable() {
        IllegalArgumentException failure =
                assertThrows(IllegalArgumentException.class, () -> SI.Enumish.Companion.valueOf("X"));
        assertEquals("No enumish entry with label 'X' in SI", failure.getMessage());
    }

    // docs/test/ケース05-境界横断.md XMP-27: @JvmStatic 非付与のため、SI.Enumish.getEntries() 形で呼べる
    // static アクセサは interface 上に存在しない（reflection で不在を固定。Companion フィールド経由が唯一の経路）
    @Test
    void noStaticAccessorsExistWithoutJvmStatic() {
        List<String> staticAccessors = Arrays.stream(SI.Enumish.class.getDeclaredMethods())
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .map(Method::getName)
                .filter(name -> name.equals("getEntries") || name.equals("valueOf") || name.equals("valueOfOrNull"))
                .toList();
        assertEquals(List.of(), staticAccessors);
    }

    // docs/test/ケース05-境界横断.md XMP-28: kind のメンバー（getLabel / getEnumizedClass /
    // getEnumishCompanion）と値側の asEnumish() / label 拡張（Java からはトップレベル static の
    // EnumizedLabelKt.getLabel）が、共変 override の bridge メソッド経由で Java から解決される
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
