package io.github.projectmapk.mpp.fixtures.jvm

import io.github.projectmapk.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// platform 専用ソースセット（jvmMain）の @Enumize が V5 非依存で正常生成されることの box テスト
// （docs/test/ケース05-境界横断.md XMP-41）
class JvmOnlyHierarchyTest {
    @Test
    fun platformOnlyHierarchyGenerates() {
        assertEquals(listOf("A", "B"), JvmOnly.Enumish.entries.map { it.label })
        assertSame(JvmOnly.A, JvmOnly.Enumish.valueOf("A"))
        assertSame(JvmOnly.B.Companion, JvmOnly.B(1).asEnumish())
        assertEquals("B", JvmOnly.B(1).label)
    }

    // kind 単位の網羅 when（else 省略）も platform 配置で成立する
    @Test
    fun kindWhenIsExhaustive() {
        val branches =
            JvmOnly.Enumish.entries.map { kind ->
                when (kind) {
                    JvmOnly.A -> "a"
                    JvmOnly.B.Companion -> "b"
                }
            }
        assertEquals(listOf("a", "b"), branches)
    }
}
