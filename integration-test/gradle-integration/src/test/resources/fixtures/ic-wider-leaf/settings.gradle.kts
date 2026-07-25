// TestKit フィクスチャ: 基底より広い末端の #7-c 系編集（docs/テストケース管理.md TC-IC-022）
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

rootProject.name = "ic-wider-leaf"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
