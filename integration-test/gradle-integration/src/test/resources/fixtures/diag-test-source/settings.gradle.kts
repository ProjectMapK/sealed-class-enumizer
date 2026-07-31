// diag-test-source フィクスチャ（docs/test/ケース04-診断.md DIA-71）:
// main の @Enumize 基底を test compilation の末端が継承する構成の言語委譲
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

rootProject.name = "diag-test-source"
