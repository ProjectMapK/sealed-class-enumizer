package org.wrongwrong.gradle

import kotlin.test.Test
import kotlin.test.assertEquals

// 残ケース掃討: レキシカル配置と可視性の編集ケース（ic-basic フィクスチャを展開先コピー上の
// 編集で再利用する。docs/テストケース管理.md TC-ORD-035・TC-VIS-046）
class SweepIcPlacementTest {
    // TC-ORD-035: 末端をトップレベルから基底本体内へ移設（ファイル配置でなくネスト配置の変更）。
    // ClassId=FQN が変わり entries の並びが再配置される一方、label と valueOf は単純名依存で不変
    @Test
    fun movingLeafIntoBaseBodyReordersEntriesButKeepsLabel() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "sworder-")
        assertEquals(IcBasicFixture.BASELINE_OUT, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))

        // Bar.kt（トップレベル宣言）を削除し、同内容の宣言を基底本体内へ移す（単純名 Bar は不変）
        TestKitHarness.deleteFile(dir, IcBasicFixture.BAR_FILE)
        TestKitHarness.replaceInFile(
            dir, IcBasicFixture.SI_FILE,
            "sealed interface SI",
            "sealed interface SI {\n    data object Bar : SI\n}",
        )
        TestKitHarness.replaceInFile(dir, IcBasicFixture.MAIN_FILE, "describe(Bar)", "describe(SI.Bar)")
        TestKitHarness.replaceInFile(dir, IcBasicFixture.USE_FILE, "    Bar -> \"bar\"", "    SI.Bar -> \"bar\"")
        val second = TestKitHarness.build(dir, "runMain")

        // FQN が p.Bar → p.SI.Bar へ変わり並びが再配置（Foo < Outer.Leaf < SI.Bar）。
        // label("Bar")・valueOf・kind-when の意味論は不変
        val expected = IcBasicFixture.BASELINE_OUT.map { line ->
            if (line.startsWith("ENTRIES=")) "ENTRIES=Foo,Leaf,Bar" else line
        }
        assertEquals(expected, IcTestSupport.outLines(second))
    }

    // TC-VIS-046: 基底の可視性を public→internal へ変更しても、entries の順序（FQN 由来）と
    // 末端側生成物（label / enumizedClass / asEnumish / companion）はバイト単位で不変
    @Test
    fun baseVisibilityChangeKeepsOrderAndLeafOutputs() {
        val dir = IcTestSupport.prepare(IcBasicFixture.NAME, "swvis46-")
        val baseline = IcTestSupport.outLines(TestKitHarness.build(dir, "runMain"))
        assertEquals(IcBasicFixture.BASELINE_OUT, baseline)
        val digests0 = IcTestSupport.classDigests(dir)

        TestKitHarness.replaceInFile(dir, IcBasicFixture.SI_FILE, "sealed interface SI", "internal sealed interface SI")
        // 基底 internal 化に伴う公開面の追随（public 関数のシグネチャに internal 型を出さない）
        TestKitHarness.replaceInFile(dir, IcBasicFixture.USE_FILE, "fun describe(value: SI)", "internal fun describe(value: SI)")
        val second = TestKitHarness.build(dir, "runMain")

        assertEquals(baseline, IcTestSupport.outLines(second))
        val digests1 = IcTestSupport.classDigests(dir)
        val leafKeys = { key: String ->
            key.startsWith("${IcBasicFixture.CLASS_PREFIX}/Foo") ||
                key.startsWith("${IcBasicFixture.CLASS_PREFIX}/Bar") ||
                key.startsWith("${IcBasicFixture.CLASS_PREFIX}/Outer")
        }
        assertEquals(digests0.filterKeys(leafKeys), digests1.filterKeys(leafKeys))
    }
}
