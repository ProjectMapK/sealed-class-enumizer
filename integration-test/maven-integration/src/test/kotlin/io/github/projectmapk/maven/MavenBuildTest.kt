package io.github.projectmapk.maven

import kotlin.test.Test
import kotlin.test.assertEquals

// 実 Maven での適用の観測（docs/test/ケース06-ビルド動態.md BLD-49〜BLD-53）。
// label は設定で変わる値であり、フィクスチャの JUnit が出す OUT 行で照合する
class MavenBuildTest {
    private data class Applied(
        val succeeded: Boolean,
        // 「Applied plugin」は 1 コンパイレーションにつき 1 行。production / test の両方に載ることの観測
        val appliedPluginCount: Int,
        val observations: List<String>,
    )

    private data class Failed(val succeeded: Boolean, val messageFound: Boolean)

    @Test
    fun appliesToProductionAndTestCompilations() {
        val result = MavenHarness.run(MavenHarness.prepareFixture("basic"), "test")
        assertEquals(
            Applied(
                succeeded = true,
                appliedPluginCount = 2,
                observations =
                    listOf(
                        // プロジェクト既定は未指定 = 組み込み既定（AS_DECLARED）
                        "OUT: si=Bar,FooBar",
                        "OUT: pinned=foo-bar",
                        "OUT: testOnly=OnlyLeaf",
                    ),
            ),
            observed(result),
            result.output,
        )
    }

    @Test
    fun projectDefaultLabelCaseReachesTheCompiler() {
        val result =
            MavenHarness.run(MavenHarness.prepareFixture("basic"), "test", "-Pproject-default")
        assertEquals(
            Applied(
                succeeded = true,
                appliedPluginCount = 2,
                observations =
                    listOf(
                        "OUT: si=BAR,FOO_BAR",
                        // 階層個別の具体指定はプロジェクト既定に勝つ
                        "OUT: pinned=foo-bar",
                        "OUT: testOnly=ONLY_LEAF",
                    ),
            ),
            observed(result),
            result.output,
        )
    }

    @Test
    fun executionLevelPluginOptionOverridesTheProjectDefault() {
        val result =
            MavenHarness.run(
                MavenHarness.prepareFixture("basic"),
                "test",
                "-Pproject-default,plugin-option",
            )
        assertEquals(
            Applied(
                succeeded = true,
                appliedPluginCount = 2,
                observations =
                    listOf("OUT: si=bar,foo_bar", "OUT: pinned=foo-bar", "OUT: testOnly=only_leaf"),
            ),
            observed(result),
            result.output,
        )
    }

    @Test
    fun compilerDiagnosticFailsTheBuild() {
        val result = MavenHarness.run(MavenHarness.prepareFixture("diag"), "compile")
        assertEquals(
            Failed(succeeded = false, messageFound = true),
            Failed(
                result.succeeded,
                result.output.contains(
                    "'@Enumize' is applicable only to a sealed class or a sealed interface."
                ),
            ),
            result.output,
        )
    }

    @Test
    fun unknownLabelCasePropertyFailsTheBuild() {
        val result =
            MavenHarness.run(MavenHarness.prepareFixture("diag"), "compile", "-Pbad-label-case")
        assertEquals(
            Failed(succeeded = false, messageFound = true),
            Failed(
                result.succeeded,
                result.output.contains(
                    "Unknown value for the 'sealed-class-enumizer.labelCase' property: " +
                        "'Upper_Snake'. Expected one of: AS_DECLARED, UPPER_SNAKE_CASE, " +
                        "SNAKE_CASE, KEBAB_CASE"
                ),
            ),
            result.output,
        )
    }

    private fun observed(result: MavenResult): Applied =
        Applied(
            succeeded = result.succeeded,
            appliedPluginCount = result.appliedPluginCount(PLUGIN_NAME),
            observations = result.observations(),
        )

    private companion object {
        // 利用側が <compilerPlugins> へ書く名前（maven-plugin の
        // SealedClassEnumizerMavenPluginExtension.PLUGIN_NAME）
        const val PLUGIN_NAME: String = "sealed-class-enumizer"
    }
}
