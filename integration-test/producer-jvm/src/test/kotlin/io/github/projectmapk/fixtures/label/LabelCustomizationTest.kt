package io.github.projectmapk.fixtures.label

import io.github.projectmapk.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

// label カスタマイズの実行時観測（docs/test/ケース01-生成と実行時API.md API-53〜API-56）
class LabelCustomizationTest {
    // docs/test/ケース01-生成と実行時API.md API-53: labelCase = UPPER_SNAKE_CASE は enum 末端の kind を
    // 含む全末端へ一律適用され、valueOf は最終 label で照合する（変換前の単純名では解決しない）
    @Test
    fun labelCaseAppliesToAllLeaves() {
        assertEquals(
            listOf("BUILTIN", "FOO_BAR", "HTTP_SERVER", "legacy-name"),
            Cased.Enumish.entries.map { it.label },
        )
        assertSame(Cased.FooBar.Companion, Cased.Enumish.valueOf("FOO_BAR"))
        assertNull(Cased.Enumish.valueOfOrNull("FooBar"))
    }

    // docs/test/ケース01-生成と実行時API.md API-54: 明示 label（@EnumishLabel）は変換より優先し、
    // kind の toString も label へ追随する。data object の toString は言語合成（単純名）のままで
    // label と乖離してよい（docs/概要.md §4 原則 1）
    @Test
    fun explicitLabelWinsAndToStringFollows() {
        assertEquals("legacy-name", Cased.Renamed(1).label)
        assertEquals(
            listOf("FOO_BAR", "legacy-name", "HTTPServer"),
            listOf(
                Cased.FooBar.Companion.toString(),
                Cased.Renamed.Companion.toString(),
                Cased.HTTPServer.toString(),
            ),
        )
        assertEquals("HTTP_SERVER", Cased.HTTPServer.label)
    }

    // docs/test/ケース01-生成と実行時API.md API-55: PROJECT_DEFAULT の明示はプロジェクト既定
    // （本モジュールは DSL 未設定 = convention の AS_DECLARED）へ解決される
    @Test
    fun projectDefaultResolvesToProjectSetting() {
        assertEquals(listOf("AlphaBeta"), ProjectDefaulted.Enumish.entries.map { it.label })
    }

    // docs/test/ケース01-生成と実行時API.md API-56: 単純名が階層内で重複する構成は明示 label で
    // 解消でき、entries の順序（ClassId 由来）は label の値に影響されない
    @Test
    fun aliasResolvesSimpleNameClash() {
        assertEquals(listOf("SameInNs", "Same"), AliasResolved.Enumish.entries.map { it.label })
        assertSame(AliasNs.Same, AliasResolved.Enumish.valueOf("SameInNs"))
    }
}
