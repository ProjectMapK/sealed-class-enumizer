// sweep-mpp-hmpp フィクスチャ（docs/test/ケース04-診断.md DIA-11 = HMPP 派生ソースセットへの末端逸脱）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.0"
        kotlin("multiplatform") version "2.4.0"
        id("io.github.projectmapk.sealed-class-enumizer") version "1.0-SNAPSHOT"
    }
}

rootProject.name = "sweep-mpp-hmpp"

