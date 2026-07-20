// sweep-typealias フィクスチャ（docs/テストケース管理.md TC-MAN-069 = typealias 経由の手動 Enumized 照合）
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

rootProject.name = "sweep-typealias"

includeBuild("%%PARENT_BUILD%%")
