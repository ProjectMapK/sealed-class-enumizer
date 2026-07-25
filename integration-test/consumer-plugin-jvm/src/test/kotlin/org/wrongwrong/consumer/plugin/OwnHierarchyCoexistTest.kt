package org.wrongwrong.consumer.plugin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import org.wrongwrong.fixtures.SI
import org.wrongwrong.sealedClassEnumizer.Enumish
import org.wrongwrong.sealedClassEnumizer.EnumishCompanion
import org.wrongwrong.sealedClassEnumizer.Enumized
import org.wrongwrong.sealedClassEnumizer.label

// 自前 @Enumize 階層（TI）と他モジュール生成 API（SI）の併用（docs/テストケース管理.md TC-XM-040 / TC-XM-056）
class OwnHierarchyCoexistTest {
    // TC-XM-040: 利用側のプラグイン適用は producer の生成 API 参照を妨げず、自前階層の生成も通常どおり動く
    @Test
    fun ownHierarchyWorksAlongsideProducerApi() {
        assertEquals(listOf("Alpha", "Beta"), TI.Enumish.entries.map { it.label })
        assertSame(TI.Alpha.Companion, TI.Alpha(1).asEnumish())
        assertSame(TI.Beta, TI.Enumish.valueOf("Beta"))
        assertEquals(listOf("Bar", "Foo"), SI.Enumish.entries.map { it.label })
    }

    // TC-XM-040: 両モジュールの階層の Companion を List<EnumishCompanion<Enumish>> として
    // 射影なしで束ねられる（EnumishCompanion<out T> の宣言側共変の跨モジュール併用）
    @Test
    fun companionsOfBothModulesBundleWithoutProjection() {
        val companions: List<EnumishCompanion<Enumish>> = listOf(SI.Enumish, TI.Enumish)
        val labels = companions.flatMap { companion -> companion.entries.map { it.label } }
        assertEquals(listOf("Bar", "Foo", "Alpha", "Beta"), labels)
    }

    // TC-XM-056（正値側）: gradle-plugin 適用側は runtime-api が自動追加されるため、
    // build.gradle.kts に明示宣言が無くても基底型（Enumized）・label 拡張が解決できる
    // （オプトアウト縮退・producer 側 implementation 隠しの比較は gradle-integration の RuntimeApiExposureTest が担当）
    @Test
    fun runtimeApiIsAutoSuppliedToPluginConsumer() {
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
