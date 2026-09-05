package io.github.projectmapk.sealedClassEnumizer.compiler

import io.github.projectmapk.sealedClassEnumizer.compiler.testing.CompileResult
import io.github.projectmapk.sealedClassEnumizer.compiler.testing.DiagAsserts.assertDiagnosticInFile
import io.github.projectmapk.sealedClassEnumizer.compiler.testing.DiagFragments
import io.github.projectmapk.sealedClassEnumizer.compiler.testing.FixtureCompilation
import io.github.projectmapk.sealedClassEnumizer.compiler.testing.FixtureCompiler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.jetbrains.kotlin.cli.common.ExitCode

// 跨 module 可視性の負値（docs/test/ケース05-境界横断.md XMP-13/15/17）。
// sweep-xm-negative フィクスチャ（producer = 可視性混在 4 階層 + consumer = 未適用）を
// producer → consumer の 2 コンパイルで観測する（producer は成立し consumer 側だけが言語エラーで壊れる）
class CrossModuleNegativeTest {
    private fun compilations(): List<CompileResult> =
        FixtureCompiler.compile(
            "sweep-xm-negative",
            listOf(
                FixtureCompilation.producer("producer"),
                FixtureCompilation.consumer("consumer"),
            ),
        )

    private fun consumerOutput(): String =
        compilations().last().also { assertFalse(it.succeeded) }.output

    // 可視性混在の階層を持つ producer 自体は正常にコンパイルされる（負値は消費側のみ）
    @Test
    fun producerCompilesCleanly() {
        val producer = compilations().first()
        assertEquals(ExitCode.OK, producer.exitCode, producer.output)
    }

    // docs/test/ケース05-境界横断.md XMP-15: 全 internal 階層は跨 module で完全不可視
    @Test
    fun internalHierarchyIsInvisibleAcrossModule() {
        assertDiagnosticInFile(consumerOutput(), "UseInternal.kt", "SwXnInt")
    }

    // docs/test/ケース05-境界横断.md XMP-13: 不可視 kind を含む when・internal 階層内手動実装を含む
    // when は else 必須（省略は言語の網羅性エラー）
    @Test
    fun elseIsRequiredForInvisibleKindsAndManualImpls() {
        val output = consumerOutput()
        assertDiagnosticInFile(output, "UseElse.kt", DiagFragments.LANG_WHEN_NOT_EXHAUSTIVE)
        assertDiagnosticInFile(output, "UseMan.kt", DiagFragments.LANG_WHEN_NOT_EXHAUSTIVE)
    }

    // docs/test/ケース05-境界横断.md XMP-17: 広い末端でも基底（sealed 親 = 生成 Enumish の内包先）が
    // 不可視の位置では階層単位の when を構成できない（単一 kind の when は成立 = フィクスチャ内で固定）
    @Test
    fun widerLeafBaseWhenCannotBeFormed() {
        assertDiagnosticInFile(consumerOutput(), "UseWide.kt", "SwXnWideBase")
    }
}
