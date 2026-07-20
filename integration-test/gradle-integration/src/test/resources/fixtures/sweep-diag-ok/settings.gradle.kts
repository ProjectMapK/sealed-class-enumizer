// sweep-diag-ok フィクスチャ（docs/テストケース管理.md 残ケース掃討・非発火 near-miss の補完）
pluginManagement {
    includeBuild("%%PARENT_BUILD%%")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
        kotlin("multiplatform") version "2.4.20-Beta1"
    }
}

rootProject.name = "sweep-diag-ok"

includeBuild("%%PARENT_BUILD%%")
