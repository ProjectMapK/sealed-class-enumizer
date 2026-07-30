package io.github.projectmapk.sealedClassEnumizer.compiler

import io.github.projectmapk.sealedClassEnumizer.LabelCase as RuntimeLabelCase
import kotlin.test.Test
import kotlin.test.assertEquals

// 変換規則は kotlinx.serialization の JsonNamingStrategy（SnakeCase / KebabCase）と同一の単語分割であり、
// label は永続化互換のためリリース後に変換結果を変えない — 期待値はこのテストで固定する（docs/概要.md §4）。
// 入力は頭字語・数字・既存区切り・単一文字・backtick 名（空白）・非 ASCII を含み、ロケール非依存の
// 変換であることを前提に期待値を列挙する
class EnumizeLabelCaseTest {
    private val inputs =
        listOf(
            "FooBar",
            "HTTPServer",
            "URLValue",
            "OAuth2Token",
            "aLower",
            "FOO_BAR",
            "ABC",
            "A",
            "Value1",
            "Foo2Bar",
            "foo bar",
            "統計データ",
        )

    @Test
    fun `エントリは runtime-api の LabelCase と対応する`() {
        // runtime-api 側は PROJECT_DEFAULT（プロジェクト既定の読み込み指定）が先頭に付くだけで、
        // 具体ケースの集合と並びは一致していなければならない
        assertEquals(
            RuntimeLabelCase.entries.map { it.name },
            listOf(RuntimeLabelCase.PROJECT_DEFAULT.name) + EnumizeLabelCase.entries.map { it.name },
        )
    }

    @Test
    fun `fromNameOrNull は具体ケースのみ解決する`() {
        assertEquals(
            listOf(EnumizeLabelCase.SNAKE_CASE, null, null),
            listOf("SNAKE_CASE", "PROJECT_DEFAULT", "snake_case").map {
                EnumizeLabelCase.fromNameOrNull(it)
            },
        )
    }

    @Test
    fun `AS_DECLARED は無変換`() {
        assertEquals(inputs, inputs.map { EnumizeLabelCase.AS_DECLARED.convert(it) })
    }

    @Test
    fun `SNAKE_CASE の変換`() {
        assertEquals(
            listOf(
                "foo_bar",
                "http_server",
                "url_value",
                "o_auth2_token",
                "a_lower",
                "foo_bar",
                "abc",
                "a",
                "value1",
                "foo2_bar",
                "foo bar",
                "統計データ",
            ),
            inputs.map { EnumizeLabelCase.SNAKE_CASE.convert(it) },
        )
    }

    @Test
    fun `UPPER_SNAKE_CASE の変換`() {
        assertEquals(
            listOf(
                "FOO_BAR",
                "HTTP_SERVER",
                "URL_VALUE",
                "O_AUTH2_TOKEN",
                "A_LOWER",
                "FOO_BAR",
                "ABC",
                "A",
                "VALUE1",
                "FOO2_BAR",
                "FOO BAR",
                "統計データ",
            ),
            inputs.map { EnumizeLabelCase.UPPER_SNAKE_CASE.convert(it) },
        )
    }

    @Test
    fun `KEBAB_CASE の変換`() {
        // "FOO_BAR" → "foo_-bar" は kotlinx.serialization と同一挙動の既知のエッジ
        // （既存の '_' は語境界を作らず、区切り記号の直後でも delimiter が挿入される）
        assertEquals(
            listOf(
                "foo-bar",
                "http-server",
                "url-value",
                "o-auth2-token",
                "a-lower",
                "foo_-bar",
                "abc",
                "a",
                "value1",
                "foo2-bar",
                "foo bar",
                "統計データ",
            ),
            inputs.map { EnumizeLabelCase.KEBAB_CASE.convert(it) },
        )
    }
}
