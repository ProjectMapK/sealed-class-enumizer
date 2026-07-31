// label-case フィクスチャ（docs/test/ケース06-ビルド動態.md BLD-48）:
// DSL の labelCase → コンパイラオプションの伝達と、@Enumize 具体指定の優先を実行時に観測する
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.0"
        id("io.github.projectmapk.sealed-class-enumizer") version "1.0-SNAPSHOT"
    }
}

rootProject.name = "label-case"
