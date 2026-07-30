package io.github.projectmapk.sealedClassEnumizer

// @Enumize の labelCase に指定する label のケース指定（docs/概要.md §4）。
// PROJECT_DEFAULT はプロジェクト既定（未設定時は AS_DECLARED）を読み込む指定であり、
// それ以外が変換の具体指定。
// 変換の単語分割は kotlinx.serialization の JsonNamingStrategy と同一であり、label は永続化互換のため
// リリース後に変換結果を変えない
enum class LabelCase {
    PROJECT_DEFAULT,
    AS_DECLARED,
    UPPER_SNAKE_CASE,
    SNAKE_CASE,
    KEBAB_CASE,
}
