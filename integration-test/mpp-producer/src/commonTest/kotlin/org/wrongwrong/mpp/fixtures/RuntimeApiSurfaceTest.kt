package org.wrongwrong.mpp.fixtures

import kotlin.test.Test
import kotlin.test.assertEquals
import org.wrongwrong.sealedClassEnumizer.Enumish
import org.wrongwrong.sealedClassEnumizer.EnumishCompanion
import org.wrongwrong.sealedClassEnumizer.Enumized
import org.wrongwrong.sealedClassEnumizer.label

// runtime-api の基底型が common から解決され全ターゲットでリンクすること
// （docs/test/ケース05-境界横断.md XMP-25）と、複数階層の Companion を宣言側共変で
// 束ねられること（docs/test/ケース05-境界横断.md XMP-22）の box テスト
class RuntimeApiSurfaceTest {
    // XMP-25: Enumish / Enumized / label 拡張が commonTest から型付きで利用できる
    @Test
    fun runtimeApiTypesResolveFromCommon() {
        val kind: Enumish = SI.Bar
        val value: Enumized<SI.Enumish> = SI.Foo(1)
        assertEquals(
            listOf("Bar", "Foo", "Foo"),
            listOf(kind.label, value.asEnumish().label, value.label),
        )
    }

    // XMP-22: List<EnumishCompanion<Enumish>> へ射影なしで束ね、各 entries を走査できる
    @Test
    fun companionsBundleCovariantly() {
        val companions: List<EnumishCompanion<Enumish>> = listOf(SI.Enumish, Command.Enumish)
        assertEquals(
            listOf(listOf("Bar", "Foo"), listOf("Builtin", "Custom")),
            companions.map { companion -> companion.entries.map { it.label } },
        )
    }
}
