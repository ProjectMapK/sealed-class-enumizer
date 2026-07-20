package org.wrongwrong.mpp.fixtures.web

import org.wrongwrong.sealedClassEnumizer.label
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.fail

// 中間ソースセット（webMain）の @Enumize 階層が、中間 metadata コンパイルを通り
// 共有先の各 platform（js / wasmJs）で動作することの box テスト
// （docs/テストケース管理.md TC-MPP-046 の web 共有代替形）
class WebSharedTest {
    // 生成 companion の名指し・末端型受け手の asEnumish() は跨コンパイレーション NG のため
    // 基底型経由 + valueOf で同一性を観測する（docs/修正方針案.md 反映待ち）
    @Test
    fun intermediateSourceSetHierarchyWorks() {
        assertEquals(listOf("W1", "W2"), WebShared.Enumish.entries.map { it.label })
        assertSame(WebShared.W1, WebShared.Enumish.valueOf("W1"))
        val w2: WebShared = WebShared.W2(0)
        assertSame(w2.asEnumish(), WebShared.Enumish.valueOf("W2"))
        assertEquals("W2", WebShared.W2(1).label)
    }

    // kind 単位の網羅 when（else 省略）の本来形。W2 の kind 枝 `WebShared.W2.Companion ->` は
    // 生成 companion の名指しであり、跨コンパイレーション未解決 NG（klib: Cannot access class）
    // のため無効化する。本来形:
    //     when (kind) {
    //         WebShared.W1 -> "w1"
    //         WebShared.W2.Companion -> "w2"
    //     }
    // NG: 共通ソースの生成 companion が跨コンパイレーションで未解決 — docs/修正方針案.md 反映待ち
    @Ignore
    @Test
    fun kindWhenIsExhaustiveWithoutElse() {
        fail("NG により無効化（本来形は上のコメントを参照）")
    }
}
