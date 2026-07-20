package org.wrongwrong.consumer.pure

import org.wrongwrong.fixtures.Command
import org.wrongwrong.fixtures.SI
import org.wrongwrong.sealedClassEnumizer.Enumish
import org.wrongwrong.sealedClassEnumizer.Enumized
import org.wrongwrong.sealedClassEnumizer.label
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

// 生成 API がプラグイン未適用モジュールから普通に参照できること（docs/概要.md §7「モジュールを跨いだ利用」・
// docs/テストケース管理.md TC-XM-001〜007 / TC-BOX-061）
class GeneratedApiVisibilityTest {
    // TC-XM-001 / TC-XM-003 / TC-BOX-061: entries・asEnumish・label 拡張の跨モジュール参照
    @Test
    fun generatedApiIsVisibleWithoutPlugin() {
        val entries: List<SI.Enumish> = SI.Enumish.entries
        assertEquals(listOf("Bar", "Foo"), entries.map { it.label })
        val si: SI = SI.Foo(1)
        assertSame(SI.Foo.Companion, si.asEnumish())
        assertEquals("Foo", si.label)
    }

    // TC-XM-001: entries は跨モジュールでも毎回同じインスタンスを返す（EntriesHolder の遅延構築は一度きり）
    @Test
    fun entriesReturnSameInstanceAcrossModule() {
        assertSame(SI.Enumish.entries, SI.Enumish.entries)
    }

    // TC-XM-002: valueOf / valueOfOrNull と失敗メッセージ（docs/概要.md §2 の文言・simpleName 由来）
    @Test
    fun valueOfResolvesWithDocumentedFailureMessage() {
        assertSame(SI.Foo.Companion, SI.Enumish.valueOf("Foo"))
        assertSame(SI.Bar, SI.Enumish.valueOfOrNull("Bar"))
        assertNull(SI.Enumish.valueOfOrNull("X"))
        val failure = assertFailsWith<IllegalArgumentException> { SI.Enumish.valueOf("X") }
        assertEquals("No enumish entry with label 'X' in SI", failure.message)
    }

    // TC-XM-004: enumizedClass の共変 override が跨モジュールで機能し List<KClass<out SI>> に型付く
    @Test
    fun enumizedClassIsCovariantlyTypedAcrossModule() {
        val classes: List<KClass<out SI>> = SI.Enumish.entries.map { it.enumizedClass }
        assertEquals(listOf(SI.Bar::class, SI.Foo::class), classes)
    }

    // TC-XM-005: enumishCompanion 経由で階層全体へ到達し、共変 override により List<SI.Enumish> として型付く
    @Test
    fun enumishCompanionNavigatesAcrossModule() {
        val kind: SI.Enumish = SI.Enumish.valueOf("Foo")
        assertSame(SI.Enumish, kind.enumishCompanion)
        val viaCompanion: List<SI.Enumish> = kind.enumishCompanion.entries
        assertSame(SI.Enumish.entries, viaCompanion)
    }

    // TC-XM-007: IR-only 生成の kind toString（V11）が跨モジュールでも仮想ディスパッチされる。
    // 末端 data object（Bar）は言語合成の toString のまま（docs/概要.md §4 原則 1）
    @Test
    fun kindToStringReturnsLabelAcrossModule() {
        assertEquals(listOf("Foo", "Bar"), listOf(SI.Foo.Companion.toString(), SI.Bar.toString()))
    }

    // TC-XM-006 読み替え: gradle-plugin の runtime-api 自動追加が implementation のため利用側へ伝播せず
    // （docs/修正方針案.md #1）、未適用消費側は build.gradle.kts の明示宣言で runtime-api を解決する。
    // その状態で基底型（Enumish / Enumized）・label 拡張・Companion 束ねが解決できることの正値観測。
    // api 公開 / implementation 縮退の比較はコンパイル失敗を要するため gradle-integration 担当
    @Test
    fun runtimeApiTypesResolveWithExplicitDeclaration() {
        val companions: List<Enumish.Companion<Enumish>> = listOf(SI.Enumish, Command.Enumish)
        val labels = companions.flatMap { companion -> companion.entries.map { it.label } }
        assertEquals(listOf("Bar", "Foo", "Builtin", "Custom"), labels)
        val value: Enumized<SI.Enumish> = SI.Foo(1)
        assertEquals("Foo", value.label)
    }
}
