// label-case フィクスチャ（docs/test/ケース06-ビルド動態.md BLD-48）:
// DSL の labelCase → コンパイラオプションの伝達と、@Enumize 具体指定の優先を実行時に観測する
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "%%KOTLIN_VERSION%%"
        id("io.github.projectmapk.sealed-class-enumizer") version "%%ENUMIZER_VERSION%%"
    }
}

rootProject.name = "label-case"
