// sweep-mpp-metadata-diag フィクスチャ（docs/test/ケース04-診断.md DIA-12 = common 診断の metadata 発火）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "%%KOTLIN_VERSION%%"
        kotlin("multiplatform") version "%%KOTLIN_VERSION%%"
        id("io.github.projectmapk.sealed-class-enumizer") version "%%ENUMIZER_VERSION%%"
    }
}

rootProject.name = "sweep-mpp-metadata-diag"

