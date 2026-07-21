// 純消費側（プラグイン未適用）。可視範囲の外側からの参照が言語エラーになることを観測する
plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":producer"))
}
