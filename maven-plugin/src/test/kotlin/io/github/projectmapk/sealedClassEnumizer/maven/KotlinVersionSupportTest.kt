package io.github.projectmapk.sealedClassEnumizer.maven

import kotlin.test.Test
import kotlin.test.assertEquals

// Kotlin バージョンのサポート判定（マイナー一致）の期待値固定。
// パッチ差・プレリリース表記・マイナー / メジャー差を覆う
class KotlinVersionSupportTest {
    private data class Case(val applied: String, val supported: String, val expected: Boolean)

    @Test
    fun minorLevelMatchDecidesSupport() {
        val cases =
            listOf(
                Case(applied = "2.4.0", supported = "2.4.0", expected = true),
                Case(applied = "2.4.10", supported = "2.4.0", expected = true),
                Case(applied = "2.4.20-Beta1", supported = "2.4.0", expected = true),
                Case(applied = "2.5.0", supported = "2.4.0", expected = false),
                Case(applied = "2.5.0-Beta1", supported = "2.4.20", expected = false),
                Case(applied = "2.3.0", supported = "2.4.0", expected = false),
                Case(applied = "3.0.0", supported = "2.4.0", expected = false),
            )
        assertEquals(
            cases.map { it.expected },
            cases.map { isSupportedKotlinVersion(it.applied, it.supported) },
        )
    }
}
