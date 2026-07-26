package org.wrongwrong.fixtures.vis.nestbase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

// ネスト基底の box テスト（docs/test/ケース02-可視性.md VIS-03/VIS-04/VIS-05/VIS-22。
// protected 基底の観測はサブクラススコープが言語上の要請のため、テスト内サブクラス Peek を経由する）
class VisNestBaseTest {
    // protected ネスト基底 Shielded をサブクラススコープから観測する（VIS-03/VIS-22）
    private class Peek : ProtectedHost() {
        fun labels(): List<String> = Shielded.Enumish.entries.map { it.label }

        fun resolve(label: String): String? = Shielded.Enumish.valueOfOrNull(label)?.label

        fun kindRoundTrips(): Boolean =
            Shielded.Off(1).asEnumish() === Shielded.Enumish.valueOf("Off")

        fun pick(flag: Boolean): String {
            val value: Shielded = if (flag) Shielded.On else Shielded.Off(9)
            return when (value.asEnumish()) {
                Shielded.On -> "on"
                Shielded.Off.Companion -> "off"
            }
        }
    }

    private data class ProtectedApiSnapshot(
        val labels: List<String>,
        val resolved: String?,
        val missing: String?,
        val kindRoundTrips: Boolean,
    )

    // docs/test/ケース02-可視性.md VIS-03: open class 内 protected 基底はサブクラススコープ内で全 API 動作
    // （消費側サブクラスの観測はケース05 が正典）
    @Test
    fun protectedNestedBaseWorksInSubclassScope() {
        val peek = Peek()
        val expected =
            ProtectedApiSnapshot(
                labels = listOf("Off", "On"),
                resolved = "On",
                missing = null,
                kindRoundTrips = true,
            )
        val actual =
            ProtectedApiSnapshot(
                labels = peek.labels(),
                resolved = peek.resolve("On"),
                missing = peek.resolve("Nope"),
                kindRoundTrips = peek.kindRoundTrips(),
            )
        assertEquals(expected, actual)
    }

    // docs/test/ケース02-可視性.md VIS-04: 非 sealed 外側クラス内の @Enumize 基底へ多段ネスト生成が成立し
    // 全 API が動作する
    @Test
    fun nestedBaseGeneratesNestedApi() {
        assertEquals(listOf("A", "B"), Outer.SI.Enumish.entries.map { it.label })
        assertSame(Outer.SI.A, Outer.SI.Enumish.valueOf("A"))
        assertSame(Outer.SI.B.Companion, Outer.SI.B(1).asEnumish())
    }

    // docs/test/ケース02-可視性.md VIS-04: valueOf 失敗文言は simpleName のみ
    // （`in SI`＝外側クラス名 Outer を含まない）をネスト形で固定。トップレベル形の正典はケース01 API-03
    @Test
    fun failureMessageOmitsOuterQualifier() {
        val failure = assertFailsWith<IllegalArgumentException> { Outer.SI.Enumish.valueOf("X") }
        assertEquals("No enumish entry with label 'X' in SI", failure.message)
    }

    // docs/test/ケース02-可視性.md VIS-05: private ネスト基底は外側クラス本体内で全 API + else 無し when が成立
    @Test
    fun privateNestedBaseWorksInsideOuterBody() {
        val outer = Outer2()
        assertEquals(listOf("X", "Y"), outer.labels())
        assertEquals("X", outer.resolve("X"))
        assertEquals(listOf("x", "y"), listOf(outer.pick(true), outer.pick(false)))
    }

    // docs/test/ケース02-可視性.md VIS-22: 全 kind 可視の位置（protected 基底=サブクラススコープ内）では
    // kind-when の else を省略できる
    @Test
    fun kindWhenNeedsNoElseInSubclassScope() {
        val peek = Peek()
        assertEquals(listOf("on", "off"), listOf(peek.pick(true), peek.pick(false)))
    }
}
