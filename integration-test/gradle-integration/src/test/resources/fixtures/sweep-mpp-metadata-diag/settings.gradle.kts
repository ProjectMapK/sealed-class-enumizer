// sweep-mpp-metadata-diag フィクスチャ（docs/テストケース管理.md TC-MPP-065 = common 診断の metadata 発火）
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

rootProject.name = "sweep-mpp-metadata-diag"

includeBuild("%%PARENT_BUILD%%")
