// sweep-xm-negative フィクスチャ（docs/test/ケース05-境界横断.md XMP-13/15/17 の跨モジュール可視性負値）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
        id("io.github.projectmapk.sealed-class-enumizer") version "1.0-SNAPSHOT"
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "sweep-xm-negative"

include(
    ":producer",
    ":consumer",
)
