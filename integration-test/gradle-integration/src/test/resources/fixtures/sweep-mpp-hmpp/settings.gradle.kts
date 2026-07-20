// sweep-mpp-hmpp フィクスチャ（docs/テストケース管理.md TC-MPP-051 = HMPP 派生ソースセットへの末端逸脱）
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

rootProject.name = "sweep-mpp-hmpp"

includeBuild("%%PARENT_BUILD%%")
