// sweep-mpp-hmpp フィクスチャ（docs/test/ケース04-診断.md DIA-11 = HMPP 派生ソースセットへの末端逸脱）
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

rootProject.name = "sweep-mpp-hmpp"

