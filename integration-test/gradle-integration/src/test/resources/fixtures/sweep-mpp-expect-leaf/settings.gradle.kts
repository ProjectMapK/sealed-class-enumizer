// sweep-mpp-expect-leaf フィクスチャ（docs/テストケース管理.md TC-MPP-049 = 末端が expect/actual の near-miss）
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

rootProject.name = "sweep-mpp-expect-leaf"

includeBuild("%%PARENT_BUILD%%")
