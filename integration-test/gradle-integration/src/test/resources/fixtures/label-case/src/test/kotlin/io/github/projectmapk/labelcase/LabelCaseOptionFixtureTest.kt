package io.github.projectmapk.labelcase

import kotlin.test.Test
import kotlin.test.assertEquals

// DSL の labelCase がコンパイラオプションとして届き、@Enumize の具体指定が優先することの実行時観測
// （docs/test/ケース06-ビルド動態.md BLD-48）
class LabelCaseOptionFixtureTest {
    @Test
    fun dslDefaultAppliesAndConcreteAnnotationWins() {
        assertEquals(listOf("baz_qux", "foo_bar"), Defaulted.Enumish.entries.map { it.label })
        assertEquals(listOf("foo-bar"), Pinned.Enumish.entries.map { it.label })
    }
}
