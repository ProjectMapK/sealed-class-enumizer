package io.github.projectmapk.sealedClassEnumizer.gradle

// プロジェクト既定として指定できる label のケース（docs/概要.md §4）。
// runtime-api の LabelCase から PROJECT_DEFAULT を除いた具体ケースと 1:1 対応する
// （本 DSL 自身がプロジェクト既定の決定者であり、「プロジェクト設定読み込み」の指定は持たない）。
// エントリ名の一致は LabelCaseTest がガードし、値はコンパイラプラグインの labelCase オプションへ
// エントリ名のまま渡される
enum class LabelCase {
    AS_DECLARED,
    UPPER_SNAKE_CASE,
    SNAKE_CASE,
    KEBAB_CASE,
}
