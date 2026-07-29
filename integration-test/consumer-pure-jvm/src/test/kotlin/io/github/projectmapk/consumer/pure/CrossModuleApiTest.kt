package io.github.projectmapk.consumer.pure

import io.github.projectmapk.fixtures.enumleaf.Command
import io.github.projectmapk.fixtures.generic.Generic
import io.github.projectmapk.fixtures.si.SI
import io.github.projectmapk.fixtures.zoo.Zoo
import io.github.projectmapk.sealedClassEnumizer.EnumishCompanion
import io.github.projectmapk.sealedClassEnumizer.Enumized
import io.github.projectmapk.sealedClassEnumizer.label
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

// プラグイン未適用モジュールからの生成 API 参照と kind API の跨 module 観測
// （docs/test/ケース05-境界横断.md XMP-01〜XMP-05。種別毎の挙動の正典はケース01）。
// runtime-api 型（Enumized / EnumishCompanion / label 拡張）を build.gradle.kts の明示宣言なしで
// 型付き利用できていること自体が、producer の api スコープ自動追加による推移解決の正値観測
// （docs/test/ケース05-境界横断.md XMP-23。縮退対照は gradle-integration の RuntimeApiExposureTest）
class CrossModuleApiTest {
    // docs/test/ケース05-境界横断.md XMP-01: entries・asEnumish・label 拡張が未適用側から参照できる
    @Test
    fun generatedApiIsVisibleWithoutPlugin() {
        val entries: List<SI.Enumish> = SI.Enumish.entries
        assertEquals(listOf("Bar", "Foo"), entries.map { it.label })
        val si: Enumized<SI.Enumish> = SI.Foo(1)
        assertSame(SI.Foo.Companion, si.asEnumish())
        assertEquals("Foo", si.label)
    }

    // docs/test/ケース05-境界横断.md XMP-01: entries は跨 module でも毎回同一インスタンスを返す
    // （EntriesHolder の遅延構築は一度きり）
    @Test
    fun entriesReturnSameInstance() {
        assertSame(SI.Enumish.entries, SI.Enumish.entries)
    }

    // docs/test/ケース05-境界横断.md XMP-02: valueOf / valueOfOrNull の跨 module 解決と失敗文言
    @Test
    fun valueOfResolvesWithDocumentedFailureMessage() {
        assertSame(SI.Foo.Companion, SI.Enumish.valueOf("Foo"))
        assertSame(SI.Bar, SI.Enumish.valueOfOrNull("Bar"))
        assertNull(SI.Enumish.valueOfOrNull("X"))
        val failure = assertFailsWith<IllegalArgumentException> { SI.Enumish.valueOf("X") }
        assertEquals("No enumish entry with label 'X' in SI", failure.message)
    }

    // docs/test/ケース05-境界横断.md XMP-03: enumizedClass の共変 override が跨 module で機能し
    // List<KClass<out SI>> に型付く
    @Test
    fun enumizedClassIsCovariantlyTyped() {
        val classes: List<KClass<out SI>> = SI.Enumish.entries.map { it.enumizedClass }
        assertEquals(listOf(SI.Bar::class, SI.Foo::class), classes)
    }

    // docs/test/ケース05-境界横断.md XMP-03: enumishCompanion 経由で階層全体へ到達し、
    // EnumishCompanion<SI.Enumish> / List<SI.Enumish> として型付く（宣言側共変）
    @Test
    fun enumishCompanionNavigatesHierarchy() {
        val kind: SI.Enumish = SI.Enumish.valueOf("Foo")
        val companion: EnumishCompanion<SI.Enumish> = kind.enumishCompanion
        assertSame(SI.Enumish, companion)
        val viaCompanion: List<SI.Enumish> = companion.entries
        assertSame(SI.Enumish.entries, viaCompanion)
    }

    // docs/test/ケース05-境界横断.md XMP-04: IR-only 生成の kind toString（V11）が跨 module でも
    // Enumish 型経由で仮想ディスパッチされる。data object 末端（Bar）は言語合成の toString のまま
    @Test
    fun kindToStringDispatchesVirtually() {
        val kinds: List<SI.Enumish> = SI.Enumish.entries
        assertEquals(listOf("Bar", "Foo"), kinds.map { it.toString() })
        assertEquals("Foo", SI.Foo.Companion.toString())
    }

    // docs/test/ケース05-境界横断.md XMP-05: enum 末端は全体で 1 kind（V4）。Enum.name と kind の label・
    // 定数 toString と kind toString が管轄別に併存し、同名 data object（HELP）の label 領域とも衝突しない
    @Test
    fun enumLeafKindIsObserved() {
        assertEquals(
            listOf("Builtin", "Custom", "HELP", "Verb"),
            Command.Enumish.entries.map { it.label },
        )
        val help: Command = Command.Builtin.HELP
        assertEquals(listOf("HELP", "Builtin"), listOf(Command.Builtin.HELP.name, help.label))
        assertEquals(
            listOf("HELP", "Builtin"),
            listOf(Command.Builtin.HELP.toString(), Command.Builtin.Companion.toString()),
        )
        assertSame(Command.Enumish.valueOf("Builtin"), Command.Builtin.HELP.asEnumish())
        assertSame(Command.HELP, Command.Enumish.valueOf("HELP"))
    }

    // docs/test/ケース05-境界横断.md XMP-05: 型パラメータ付き末端の kind は型引数に依存せず、
    // enumizedClass は star projection 相当の末端 class リテラル
    @Test
    fun typeParameterizedLeafSharesKind() {
        assertSame(Generic.Box(1).asEnumish(), Generic.Box("text").asEnumish())
        assertSame(Generic.Box.Companion, Generic.Box(1).asEnumish())
        assertEquals(Generic.Box::class, Generic.Box.Companion.enumizedClass)
        assertEquals(listOf("Box", "Empty", "Fixed"), Generic.Enumish.entries.map { it.label })
    }

    // docs/test/ケース05-境界横断.md XMP-05: value class 末端は基底型で受けると boxing されるが、
    // kind は boxing 有無・underlying 値に依らず companion のシングルトンで安定する
    @Test
    fun valueClassLeafKeepsKindIdentity() {
        val boxed: Zoo = Zoo.ValueLeaf(1)
        assertSame(Zoo.ValueLeaf(2).asEnumish(), boxed.asEnumish())
        assertSame(Zoo.ValueLeaf.Companion, boxed.asEnumish())
        assertEquals(
            listOf("ValueLeaf", "ValueLeaf"),
            listOf(boxed.label, Zoo.ValueLeaf.Companion.enumizedClass.simpleName ?: ""),
        )
    }

    // docs/test/ケース05-境界横断.md XMP-05: enumizedClass は KClass キーのマップ構築や simpleName の
    // 読み取りといった reflection 連携の接続点として使える
    @Test
    fun enumizedClassConnectsReflection() {
        val byClass: Map<KClass<out SI>, SI.Enumish> =
            SI.Enumish.entries.associateBy { it.enumizedClass }
        assertSame(SI.Foo.Companion, byClass.getValue(SI.Foo::class))
        assertEquals(
            listOf("Bar", "Foo"),
            SI.Enumish.entries.map { it.enumizedClass.simpleName ?: "" },
        )
    }
}
