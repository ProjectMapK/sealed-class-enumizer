// sweep-xm-negative フィクスチャ（docs/test/ケース05-境界横断.md XMP-13/15/17 の跨モジュール可視性負値）
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
