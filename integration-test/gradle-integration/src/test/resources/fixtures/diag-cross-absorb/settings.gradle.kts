// diag-cross-absorb フィクスチャ（docs/test/フィクスチャ構成.md §4「TestKit 運用方針」）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
        kotlin("multiplatform") version "2.4.20-Beta1"
        id("io.github.projectmapk.sealed-class-enumizer") version "1.0-SNAPSHOT"
    }
}

rootProject.name = "diag-cross-absorb"

include(":lib", ":app")
