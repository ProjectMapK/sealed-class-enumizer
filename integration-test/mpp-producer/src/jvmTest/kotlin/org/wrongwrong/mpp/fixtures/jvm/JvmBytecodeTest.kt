package org.wrongwrong.mpp.fixtures.jvm

import org.wrongwrong.mpp.fixtures.SI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// JVM 固有のバイトコード観測（docs/テストケース管理.md TC-MPP-031/035/036 と TC-MPP-010 の
// bridge 半面）。テストコードからの java.lang.reflect 利用は no-reflection 原則
// （runtime-api / 生成コード側の制約）の対象外である
class JvmBytecodeTest {
    // TC-MPP-031: jvmTarget 17+ で基底 sealed に PermittedSubclasses 属性が出力される
    @Test
    fun sealedBaseHasPermittedSubclasses() {
        val si = SI::class.java
        assertTrue(si.isSealed)
        assertEquals(
            setOf(SI.Foo::class.java, SI.Bar::class.java),
            si.permittedSubclasses.toSet(),
        )
    }

    // TC-MPP-031: 生成 Enumish（sealed。V1 成立）にも PermittedSubclasses が出る
    @Test
    fun generatedEnumishIsSealedWithPermittedSubclasses() {
        val enumish = SI.Enumish::class.java
        assertTrue(enumish.isSealed)
        assertEquals(
            setOf(SI.Foo.Companion::class.java, SI.Bar::class.java),
            enumish.permittedSubclasses.toSet(),
        )
    }

    // TC-MPP-035: $EntriesHolder は @Metadata に載らず Kotlin からは不可視だが、JVM では
    // $ 入り出力名のネストクラスとして Java から見える（ABI 保証外）
    @Test
    fun entriesHolderIsJavaVisibleWithDollarName() {
        val holder = SI.Enumish::class.java.declaredClasses.firstOrNull { it.name.contains("EntriesHolder") }
        assertNotNull(
            holder,
            "declaredClasses=${SI.Enumish::class.java.declaredClasses.map { it.name }}",
        )
        assertTrue(holder.simpleName.startsWith("$"), "simpleName=${holder.simpleName}")
    }

    // TC-MPP-036/010: 共変 override（enumishCompanion / asEnumish）にコンパイラ本体が
    // bridge メソッドを生成する（プラグイン非関与の JVM 固有 lowering）。
    // enumishCompanion の実装本体は interface（SI.Enumish）の default メソッド側にあり、
    // 実装クラス側（Bar）へは delegation stub が bridge として生成される（実測形）
    @Test
    fun covariantOverridesHaveBridges() {
        val interfaceGetters = SI.Enumish::class.java.methods.filter { it.name == "getEnumishCompanion" }
        assertTrue(
            interfaceGetters.any { !it.isBridge && it.returnType == SI.Enumish.Companion::class.java },
            "methods=$interfaceGetters",
        )
        val barGetters = SI.Bar::class.java.methods.filter { it.name == "getEnumishCompanion" }
        assertTrue(barGetters.any { it.isBridge }, "methods=$barGetters")
        val asEnumishMethods = SI.Foo::class.java.methods.filter { it.name == "asEnumish" }
        assertTrue(asEnumishMethods.any { it.isBridge }, "methods=$asEnumishMethods")
        assertTrue(asEnumishMethods.any { !it.isBridge }, "methods=$asEnumishMethods")
    }
}
