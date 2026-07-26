package org.wrongwrong.fixtures.manual.impl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

// 手動実装の許容形・非掲載・登載境界・可視性の box テスト
// （docs/test/ケース01-生成と実行時API.md API-40/API-41/API-42）
class ManualImplTest {
    private data class ManualSnapshot(
        val labels: List<String>,
        val manualValueListed: Boolean,
        val manualLabelResolved: String?,
        val manualValueLabel: String,
    )

    // docs/test/ケース01-生成と実行時API.md API-40: 階層内の生成 Enumish 手動実装（open・internal 両形）は
    // 許容（このファイル群のコンパイル成功が兼ねる）。class 形（ManualLeaf）の手動実装インスタンスは
    // kind ではなく entries / valueOf に現れず、Enumish としては機能する。
    // internal object 形（ManualHidden）は kind=自身のため entries には kind として掲載される
    @Test
    fun manualImplIsAllowedButUnlisted() {
        val manualValue: WithManual.Enumish = ManualLeaf(1)
        val expected =
            ManualSnapshot(
                labels = listOf("ManualHidden", "ManualLeaf", "Real"),
                manualValueListed = false,
                manualLabelResolved = null,
                manualValueLabel = "manual-value",
            )
        val actual =
            ManualSnapshot(
                labels = WithManual.Enumish.entries.map { it.label },
                manualValueListed = WithManual.Enumish.entries.any { it === manualValue },
                manualLabelResolved = WithManual.Enumish.valueOfOrNull("manual-value")?.label,
                manualValueLabel = manualValue.label,
            )
        assertEquals(expected, actual)
    }

    // docs/test/ケース01-生成と実行時API.md API-41: 手動実装（ManualLeaf）は inheritors に登載され、
    // kind 単位の網羅 when には is 枝が必要になる（else 無しで成立すること自体が登載の検査）
    @Test
    fun kindWhenRequiresManualBranch() {
        val branches =
            WithManual.Enumish.entries.map { kind ->
                when (kind) {
                    WithManual.Real -> "real"
                    ManualHidden -> "hidden"
                    ManualLeaf.Companion -> "leaf-kind"
                    is ManualLeaf -> "manual"
                }
            }
        assertEquals(listOf("hidden", "leaf-kind", "real"), branches)
    }

    // docs/test/ケース01-生成と実行時API.md API-41: open 手動実装のサブタイプ（ManualSub）は
    // 生成 Enumish を直接実装しないため inheritors に追加されず（sealedSubclasses の全量一致で固定）、
    // 既存の is ManualLeaf 枝が被覆する
    @Test
    fun manualImplSubtypeDoesNotJoinInheritors() {
        assertEquals(
            listOf(
                ManualHidden::class,
                ManualLeaf::class,
                ManualLeaf.Companion::class,
                WithManual.Real::class,
            ),
            WithManual.Enumish::class.sealedSubclasses,
        )
        val sub: WithManual.Enumish = ManualSub(2)
        val branch =
            when (sub) {
                WithManual.Real -> "real"
                ManualHidden -> "hidden"
                ManualLeaf.Companion -> "leaf-kind"
                is ManualLeaf -> "manual"
            }
        assertEquals("manual", branch)
    }

    // docs/test/ケース01-生成と実行時API.md API-42: 基底 Enumish（runtime-api）の自由実装（階層外）は
    // 無制約で、どの階層の entries / valueOf にも現れない
    @Test
    fun freeBaseEnumishImplIsUnrestricted() {
        assertEquals("FreeAgent", FreeAgent.label)
        val agent: Any = FreeAgent
        assertFalse(WithManual.Enumish.entries.any { it === agent })
        assertNull(WithManual.Enumish.valueOfOrNull("FreeAgent"))
    }
}
