package io.github.projectmapk.sealedClassEnumizer.maven

import io.github.projectmapk.sealedClassEnumizer.LabelCase as RuntimeLabelCase
import kotlin.test.Test
import kotlin.test.assertEquals

// 受け付ける LabelCase は runtime-api の LabelCase から PROJECT_DEFAULT を除いた具体ケースと 1:1 対応する
// （docs/概要.md §4）。宣言はモジュール毎に独立させているため、名前集合と並びの一致をここで担保する:
// runtime-api 側のエントリを追加・改名すると、参照が追随するこのテストが失敗し、更新漏れを検知できる
class LabelCaseTest {
    @Test
    fun `受け付ける LabelCase は runtime-api の具体ケースと一致する`() {
        assertEquals(
            RuntimeLabelCase.entries
                .filterNot { it == RuntimeLabelCase.PROJECT_DEFAULT }
                .map { it.name },
            LabelCase.entries.map { it.name },
        )
    }
}
