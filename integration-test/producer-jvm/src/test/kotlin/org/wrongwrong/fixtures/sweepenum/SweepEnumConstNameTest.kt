package org.wrongwrong.fixtures.sweepenum

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// enum 定数名と末端単純名の非衝突 near-miss（docs/テストケース管理.md TC-MAN-078）。
// このフィクスチャがコンパイルできること自体が LABEL_CLASH 非発火の実証で、衝突判定の対象が
// enum class 宣言名（"Builtin"）であって定数名（"HELP"）でないことを実行時にも観測する
class SweepEnumConstNameTest {
    // labels は {"Builtin", "HELP"} で衝突なし（FQN 順 = Builtin < HELP）
    @Test
    fun enumConstantNameDoesNotJoinLabelDomain() {
        assertEquals(listOf("Builtin", "HELP"), SweepCmd.Enumish.entries.map { it.label })
    }

    // valueOf("HELP") は末端 object を返し、enum 定数の kind は valueOf("Builtin") で解決される
    @Test
    fun valueOfSeparatesEnumKindFromObjectLeaf() {
        assertSame(SweepCmd.HELP, SweepCmd.Enumish.valueOf("HELP"))
        assertSame(SweepCmd.Builtin.HELP.asEnumish(), SweepCmd.Enumish.valueOf("Builtin"))
    }
}
