package io.github.projectmapk.mpp.fixtures.kindaccessor

import kotlin.test.Test
import kotlin.test.assertEquals

// 参照不能 kind が IR-only アクセサ経由で全 klib ターゲットの entries / valueOf に載ることの box テスト
// （docs/test/ケース05-境界横断.md XMP-44・docs/概要.md §8・docs/コンパイラプラグイン設計02.md §4.3）。
// カバーするアクセサ配置:
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
    fun inaccessibleKindsResolvableByLabel() {
        assertEquals(
            listOf("Leaf", "KaIface", "KaPrivComp", "KaPrivTop", "KaProtComp"),
            listOf("Leaf", "KaIface", "KaPrivComp", "KaPrivTop", "KaProtComp").map {
                KaRoot.Enumish.valueOf(it).label
            },
        )
    }
}
