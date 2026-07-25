// sweep-xm-negative フィクスチャ（docs/テストケース管理.md 残ケース掃討・跨モジュール可視性の負値）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
        id("org.wrongwrong.sealed-class-enumizer") version "1.0-SNAPSHOT"
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
