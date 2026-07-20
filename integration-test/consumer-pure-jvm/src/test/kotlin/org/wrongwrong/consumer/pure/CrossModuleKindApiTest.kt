package org.wrongwrong.consumer.pure

import org.wrongwrong.fixtures.Command
import org.wrongwrong.fixtures.Generic
import org.wrongwrong.fixtures.SI
import org.wrongwrong.fixtures.valueclass.Valued
import org.wrongwrong.fixtures.valueclass.Wrapped
import org.wrongwrong.sealedClassEnumizer.label
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// kind 側 API（enumizedClass / label / toString）の跨モジュール観測
// （docs/概要.md §2、docs/テストケース管理.md TC-XM-033 / TC-XM-034 / TC-XM-041 / TC-XM-050）
class CrossModuleKindApiTest {
    // TC-XM-033: enumizedClass で末端 KClass を取得でき、DI / serialization 風の接続点
    //（KClass をキーにしたマップ構築・simpleName の読み取り）として使える
    @Test
    fun enumizedClassConnectsKindsToReflectionSystems() {
        val byClass: Map<KClass<out SI>, SI.Enumish> = SI.Enumish.entries.associateBy { it.enumizedClass }
        assertSame(SI.Foo.Companion, byClass.getValue(SI.Foo::class))
        assertEquals(listOf("Bar", "Foo"), SI.Enumish.entries.map { it.enumizedClass.simpleName ?: "" })
    }

    // TC-XM-034: 型パラメータ付き末端の kind は型引数に依存せず、enumizedClass は star projection の
    // 末端 class リテラル。生成 Enumish（Generic.Enumish）は型パラメータを持たない
    @Test
    fun typeParameterizedLeafSharesKindAcrossModule() {
        assertSame(Generic.Box(1).asEnumish(), Generic.Box("text").asEnumish())
        assertSame(Generic.Box.Companion, Generic.Box(1).asEnumish())
        assertEquals(Generic.Box::class, Generic.Box.Companion.enumizedClass)
        assertEquals(listOf("Box", "Empty"), Generic.Enumish.entries.map { it.label })
    }

    // TC-XM-050: value class 末端はインターフェース型で受けると boxing されるが、
    // kind は boxing 有無・underlying 値に依らず companion のシングルトンで安定する
    @Test
    fun valueClassLeafKeepsKindIdentityAcrossModule() {
        val boxed: Valued = Wrapped(1)
        assertSame(Wrapped(2).asEnumish(), boxed.asEnumish())
        assertSame(Wrapped.Companion, boxed.asEnumish())
        assertEquals(
            listOf("Wrapped", "Wrapped"),
            listOf(boxed.label, Wrapped.Companion.enumizedClass.simpleName ?: ""),
        )
    }

    // TC-XM-041: enum class 末端は全体で 1 kind（V4）。Enum.name と kind の label、定数の toString と
    // kind の toString（IR-only 生成 = V11）が管轄の異なる別物として跨モジュールでも併存する
    @Test
    fun enumLeafKindIsObservedAcrossModule() {
        assertEquals(listOf("Builtin", "Custom"), Command.Enumish.entries.map { it.label })
        val help: Command = Command.Builtin.HELP
        assertEquals(listOf("HELP", "Builtin"), listOf(Command.Builtin.HELP.name, help.label))
        assertEquals(
            listOf("HELP", "Builtin"),
            listOf(Command.Builtin.HELP.toString(), Command.Builtin.Companion.toString()),
        )
        assertSame(Command.Enumish.valueOf("Builtin"), Command.Builtin.HELP.asEnumish())
    }
}
