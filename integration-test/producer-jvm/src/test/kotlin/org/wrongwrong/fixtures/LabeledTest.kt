package org.wrongwrong.fixtures

import kotlin.test.Test
import kotlin.test.assertEquals
import org.wrongwrong.sealedClassEnumizer.label

// toString の 2 原則（docs/概要.md §4）と label シャドーイング（同 §2 の注意）の box テスト
class LabeledTest {
    // 原則 1: kind 自身の手動 toString（末端 object / companion とも）を尊重して生成しない
    @Test
    fun manualToStringIsRespected() {
        assertEquals(
            listOf("custom", "companion-custom"),
            listOf(Labeled.Manual.toString(), Labeled.Styled.Companion.toString()),
        )
    }

    // 手動 toString があっても label は通常どおり生成される
    @Test
    fun labelIsGeneratedRegardlessOfManualToString() {
        assertEquals(
            listOf("Manual", "Styled"),
            listOf(Labeled.Manual.label, Labeled.Styled.Companion.label),
        )
    }

    // メンバー label は拡張 label を黙って隠す — 確実な取得は asEnumish() 経由（docs/概要.md §2）
    @Test
    fun memberLabelShadowsExtensionAtCallSite() {
        val tagged = Labeled.Tagged(label = "user-value")
        assertEquals(listOf("user-value", "Tagged"), listOf(tagged.label, tagged.asEnumish().label))
    }

    // シャドーイング構成でも entries / valueOf は kind の label で解決される
    @Test
    fun valueOfUsesKindLabelNotMemberValue() {
        assertEquals(listOf("Manual", "Styled", "Tagged"), Labeled.Enumish.entries.map { it.label })
    }
}
