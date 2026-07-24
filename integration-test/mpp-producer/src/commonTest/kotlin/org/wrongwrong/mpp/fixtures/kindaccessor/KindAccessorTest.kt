package org.wrongwrong.mpp.fixtures.kindaccessor

import kotlin.test.Test
import kotlin.test.assertEquals

// 参照不能 kind が IR-only アクセサ経由で全 klib ターゲットの entries / valueOf に載ることの box テスト
// （概要 §8・設計02 §4.3）。カバーするアクセサ配置:
//   - 末端クラス内ネスト: KaPrivComp（private companion）・KaProtComp（protected companion）
//   - 末端 interface 内ネスト: KaIface（private companion）
//   - トップレベル: KaHiddenHost.Leaf（private 外側クラス）・KaPrivTop（private トップレベル末端）
class KindAccessorTest {
    @Test
    fun inaccessibleKindsAreServedThroughEntries() {
        assertEquals(
            listOf("Leaf", "KaIface", "KaPrivComp", "KaPrivTop", "KaProtComp", "Visible"),
            KaRoot.Enumish.entries.map { it.label },
        )
    }

    @Test
    fun inaccessibleKindsAreResolvableByLabel() {
        assertEquals(
            listOf("Leaf", "KaIface", "KaPrivComp", "KaPrivTop", "KaProtComp"),
            listOf("Leaf", "KaIface", "KaPrivComp", "KaPrivTop", "KaProtComp")
                .map { KaRoot.Enumish.valueOf(it).label },
        )
    }
}
