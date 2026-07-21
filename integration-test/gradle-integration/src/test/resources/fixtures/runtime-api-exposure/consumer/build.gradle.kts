// 純消費側（プラグイン未適用・runtime-api を明示宣言しない）。
// producer が runtime-api を api 公開していれば、生成 API（supertype = runtime-api の型）を解決できる
plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":producer"))
}
