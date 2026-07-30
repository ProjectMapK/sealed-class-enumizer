package io.github.projectmapk.sealedClassEnumizer.gradle

// プロジェクト既定として指定できる label のケース（docs/概要.md §4）。
// runtime-api の LabelCase から PROJECT_DEFAULT を除いた具体ケースと 1:1 対応する
// （プロジェクト既定の値はこの DSL で指定するものであり、「プロジェクト既定の読み込み」という指定は自己参照になるため持たない）。
// エントリ名の一致は LabelCaseTest がガードし、値はコンパイラプラグインの labelCase オプションへ
// エントリ名のまま渡される
enum class LabelCase {
    AS_DECLARED,
    UPPER_SNAKE_CASE,
    SNAKE_CASE,
    KEBAB_CASE,
}
