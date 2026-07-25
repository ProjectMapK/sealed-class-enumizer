package org.wrongwrong.fixtures.secret

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// 基底本体にネストした private / internal 末端の box テスト
// （docs/テストケース管理.md TC-LEAF-088 / TC-BOX-073 / TC-ORD-059 / TC-VIS-012 / TC-VIS-033 / TC-VIS-044）
class SecretTest {
    // TC-LEAF-088 / TC-BOX-073: 基底内ネストの private 末端は基底本体から参照可能でアクセサ不要（直接参照。
    // このフィクスチャがコンパイルできること自体が証明）、entries に載り valueOf で解決できる
    @Test
    fun privateNestedLeavesAppearInEntries() {
        assertNotNull(Sec.Enumish.valueOfOrNull("Zzz"))
        assertNotNull(Sec.Enumish.valueOfOrNull("Cls"))
        assertEquals("Zzz", Sec.Enumish.valueOf("Zzz").label)
    }

    // TC-ORD-059 / TC-VIS-044: 可視性は entries の順序にも掲載にも影響しない（FQN 序数順のまま）
    @Test
    fun visibilityDoesNotAffectOrderOrMembership() {
        assertEquals(listOf("Aaa", "Cls", "Mmm", "Zzz"), Sec.Enumish.entries.map { it.label })
    }

    // 可視範囲の外（このテストファイル）からも label は entries 経由で観測される（docs/エッジケースへの対応方針.md §1.2 の帰結）
    @Test
    fun labelsAreObservableOutsideVisibilityScope() {
        assertEquals(4, Sec.Enumish.entries.size)
    }
}
