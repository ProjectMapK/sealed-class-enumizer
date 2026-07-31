// diag-cross フィクスチャ（docs/test/ケース04-診断.md DIA-18/21/22/57）:
// lib（適用）+ app（適用消費）+ app2（未適用消費）の跨 module 負値を --continue の
// 1 buildAndFail で束ねて観測する
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

rootProject.name = "diag-cross"

include(":lib", ":app", ":app2")
