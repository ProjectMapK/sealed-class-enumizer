package io.github.projectmapk.consumer.plugin

import io.github.projectmapk.fixtures.si.SI
import io.github.projectmapk.sealedClassEnumizer.Enumish
import io.github.projectmapk.sealedClassEnumizer.EnumishCompanion
import io.github.projectmapk.sealedClassEnumizer.Enumized
import io.github.projectmapk.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 自前 @Enumize 階層（TI）と他モジュール生成 API（SI）の併用
// （docs/test/ケース05-境界横断.md XMP-21 / XMP-22 / XMP-25）
class OwnHierarchyCoexistTest {
    // docs/test/ケース05-境界横断.md XMP-21: 利用側のプラグイン適用は producer の生成 API 参照を妨げず、
    // 自前階層の生成も通常どおり動く（拡張登録・述語の非干渉）
    @Test
    fun ownHierarchyWorksAlongsideProducerApi() {
        assertEquals(listOf("Alpha", "Beta"), TI.Enumish.entries.map { it.label })
        assertSame(TI.Alpha.Companion, TI.Alpha(1).asEnumish())
        assertSame(TI.Beta, TI.Enumish.valueOf("Beta"))
        assertEquals(listOf("Bar", "Foo"), SI.Enumish.entries.map { it.label })
    }

    // docs/test/ケース05-境界横断.md XMP-22: 両モジュールの階層の Companion を
    // List<EnumishCompanion<Enumish>> として射影なしで束ねられる（EnumishCompanion<out T> の宣言側共変。
    // common 全ターゲット面は mpp-producer の RuntimeApiSurfaceTest が担う）
    @Test
    fun companionsBundleWithoutProjection() {
        val companions: List<EnumishCompanion<Enumish>> = listOf(SI.Enumish, TI.Enumish)
        val labels = companions.flatMap { companion -> companion.entries.map { it.label } }
        assertEquals(listOf("Bar", "Foo", "Alpha", "Beta"), labels)
    }

    // docs/test/ケース05-境界横断.md XMP-25: gradle-plugin 適用側は runtime-api が自動追加されるため、
    // build.gradle.kts に明示宣言が無くても基底型（Enumized）・label 拡張が解決できる
    // （オプトアウト縮退の対照 = XMP-24 は gradle-integration の RuntimeApiExposureTest が担う）
    @Test
    fun runtimeApiIsAutoSupplied() {
        val value: Enumized<TI.Enumish> = TI.Alpha(7)
        assertEquals("Alpha", value.label)
        val beta: TI = TI.Beta
        val branch =
            when (beta.asEnumish()) {
                TI.Alpha.Companion -> "alpha"
                TI.Beta -> "beta"
            }
        assertEquals("beta", branch)
    }
}
