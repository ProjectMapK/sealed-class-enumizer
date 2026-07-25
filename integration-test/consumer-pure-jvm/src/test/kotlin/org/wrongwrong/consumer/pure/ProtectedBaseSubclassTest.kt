package org.wrongwrong.consumer.pure

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.fixtures.protectedbase.ProtectedHost
import org.wrongwrong.sealedClassEnumizer.label

// TC-VIS-008: protected ネスト基底を別モジュールのサブクラス文脈から利用する
// （docs/エッジケースへの対応方針.md §1.2「基底の可視性は制限ではない」の protected 版）。
// protected の可視範囲（ProtectedHost とそのサブクラス）内では階層 API がすべて通常どおり使える
class ProtectedBaseSubclassTest : ProtectedHost() {
    // entries / valueOf / label 拡張がサブクラス文脈で解決される（FQN 順 = [Off, On]）
    @Test
    fun hierarchyApiWorksInSubclassContext() {
        assertEquals(listOf("Off", "On"), Shielded.Enumish.entries.map { it.label })
        assertSame(Shielded.On, Shielded.Enumish.valueOf("On"))
        val value: Shielded = Shielded.Off(1)
        assertEquals("Off", value.label)
    }

    // kind 単位の網羅 when（else 省略）もサブクラス文脈で成立する
    @Test
    fun kindWhenIsExhaustiveInSubclassContext() {
        val value: Shielded = Shielded.Off(2)
        val result =
            when (value.asEnumish()) {
                Shielded.Off.Companion -> "off"
                Shielded.On -> "on"
            }
        assertEquals("off", result)
    }
}
