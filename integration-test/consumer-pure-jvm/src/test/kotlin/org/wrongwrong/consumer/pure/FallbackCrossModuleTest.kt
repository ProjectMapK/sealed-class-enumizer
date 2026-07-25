package org.wrongwrong.consumer.pure

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.fixtures.Mixed

// public 末端 + internal companion の規則 2 フォールバックの跨モジュール観測
// （docs/コンパイラプラグイン設計01.md §5.4 規則 2・docs/エッジケースへの対応方針.md §1.3、
//  docs/テストケース管理.md TC-XM-032）
class FallbackCrossModuleTest {
    // TC-XM-032: asEnumish の返り値型は具体型ではなく Mixed.Enumish へフォールバックしており
    // （この型で受けられること自体が ABI に SI.Enumish 相当が現れる検査）、Mixed.Half.Companion は
    // internal のため跨モジュールで名指しできず、kind の突き合わせは entries / valueOf の同一性で行う
    @Test
    fun asEnumishFallsBackToEnumishTypeAcrossModule() {
        val kind: Mixed.Enumish = Mixed.Half(1).asEnumish()
        assertSame(Mixed.Enumish.valueOf("Half"), kind)
        assertSame(Mixed.Half(2).asEnumish(), kind)
        assertEquals(listOf("Full", "Half"), Mixed.Enumish.entries.map { it.label })
    }
}
