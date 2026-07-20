// sweep-xm-negative フィクスチャ（docs/テストケース管理.md 残ケース掃討・跨モジュール可視性の負値）
pluginManagement {
    includeBuild("%%PARENT_BUILD%%")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "sweep-xm-negative"

includeBuild("%%PARENT_BUILD%%")

include(
    ":producer",
    ":consumer",
)
