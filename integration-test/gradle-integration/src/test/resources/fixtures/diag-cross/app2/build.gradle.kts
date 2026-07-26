// 未適用消費側（プラグイン診断が出ない対照。docs/test/ケース04-診断.md DIA-21）
plugins {
    kotlin("jvm")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
}
