package io.github.projectmapk.gradle

import kotlin.test.Test

// プロジェクト既定（DSL の labelCase）→ コンパイラオプションの伝達と、@Enumize 具体指定の優先
// （docs/test/ケース06-ビルド動態.md BLD-48）。ラベル値の検証は label-case フィクスチャ内の JUnit が
// 担い、本テストはその test タスクの成功を観測する
class LabelCaseOptionTest : DiagTestBase() {
    @Test
    fun dslDefaultPropagatesToCompilerOption() {
        successOutput("label-case", "test")
    }
}
