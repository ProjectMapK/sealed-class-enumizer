// プラグイン適用の消費側・正値基線（docs/test/テスト戦略.md §4）。
// 負値診断（コンパイル失敗を要するもの）は gradle-integration の TestKit 側に置く
plugins {
    kotlin("jvm")
    id("org.wrongwrong.sealed-class-enumizer")
}

group = "org.wrongwrong"

version = "1.0-SNAPSHOT"

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":producer-jvm"))
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }
