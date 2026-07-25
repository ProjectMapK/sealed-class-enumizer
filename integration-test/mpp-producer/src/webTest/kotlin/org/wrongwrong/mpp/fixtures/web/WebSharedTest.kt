package org.wrongwrong.mpp.fixtures.web

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 中間ソースセット（webMain）の @Enumize 階層が、中間 metadata コンパイルを通り
// 共有先の各 platform（js / wasmJs）で動作することの box テスト
// （docs/テストケース管理.md TC-MPP-046 の web 共有代替形）
class WebSharedTest {
    // 生成 companion の名指し・末端型受け手の asEnumish() が中間ソースセット由来の階層でも成立する
    @Test
    fun intermediateSourceSetHierarchyWorks() {
        assertEquals(listOf("W1", "W2"), WebShared.Enumish.entries.map { it.label })
        assertSame(WebShared.W1, WebShared.Enumish.valueOf("W1"))
        assertSame(WebShared.W2.Companion, WebShared.W2(0).asEnumish())
        assertEquals("W2", WebShared.W2(1).label)
    }

    // kind 単位の網羅 when（else 省略）。W2 の kind 枝は生成 companion の名指しで書く
    @Test
    fun kindWhenIsExhaustiveWithoutElse() {
        val branches = WebShared.Enumish.entries.map { kind ->
            when (kind) {
                WebShared.W1 -> "w1"
                WebShared.W2.Companion -> "w2"
            }
        }
        assertEquals(listOf("w1", "w2"), branches)
    }
}
