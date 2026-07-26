package org.wrongwrong.fixtures.sealedbase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// sealed class 基底・コンストラクタ形・protected 末端・基底 companion 末端の box テスト
// （docs/test/ケース01-生成と実行時API.md §6・docs/test/ケース02-可視性.md VIS-09）
class SealedClassBaseTest {
    // docs/test/ケース01-生成と実行時API.md API-27: `:Task()` 形 supertype でも所属判定・
    // companion 自動生成が成立する
    @Test
    fun constructorCallSupertypeWorks() {
        assertSame(Task.Run.Companion, Task.Run(1).asEnumish())
        assertSame(Task.Run.Companion, Task.Enumish.valueOf("Run"))
        assertEquals(
            listOf("Run", Task.Run::class),
            listOf(Task.Run.Companion.label, Task.Run.Companion.enumizedClass),
        )
    }

    // docs/test/ケース01-生成と実行時API.md API-28: sealed class 基底の object / data object 末端は
    // 自身が kind。toString は非 data = 生成 / data = 言語合成の差分
    @Test
    fun objectLeavesOfClassBase() {
        assertSame(Task.Plain, Task.Plain.asEnumish())
        assertSame(Task.Done, Task.Done.asEnumish())
        assertEquals(listOf("Plain", "Done"), listOf(Task.Plain.toString(), Task.Done.toString()))
    }

    // docs/test/ケース01-生成と実行時API.md API-52: 基底自身の companion（`companion object : Task()`）は
    // 末端として成立する（COMPANION_LEAF_CONFLICT は外側 = 末端のみ検査）。kind = 自身・label = "Companion"
    @Test
    fun baseCompanionLeafIsAllowed() {
        // 基底 companion は階層の値でもある（型付き代入が成立の検査）
        val asValue: Task = Task.Companion
        assertSame(Task.Companion, asValue.asEnumish())
        assertEquals("Companion", Task.Companion.label)
        assertSame(Task.Companion, Task.Enumish.valueOf("Companion"))
    }

    // docs/test/ケース02-可視性.md VIS-09: protected 末端は基底スコープ内の直接参照で掲載され、
    // 明示 companion は規則 1 の具体型（観測は基底 companion の innerKind = 基底スコープ内で型付け済み）
    @Test
    fun protectedLeafIsListedWithConcreteReturnType() {
        assertEquals(
            listOf("Companion", "Done", "Inner", "Plain", "Run"),
            Task.Enumish.entries.map { it.label },
        )
        assertSame(Task.Enumish.valueOf("Inner"), Task.innerKind)
        assertSame(Task.innerKind, Task.makeInner().asEnumish())
    }
}
