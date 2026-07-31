// probe-annotation-alias フィクスチャ（docs/test/ケース04-診断.md DIA-67 実挙動固定）
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

rootProject.name = "probe-annotation-alias"
