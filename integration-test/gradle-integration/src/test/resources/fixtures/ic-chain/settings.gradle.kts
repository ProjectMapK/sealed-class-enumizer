// TestKit フィクスチャ: 多段中間 sealed チェーン（docs/テストケース管理.md TC-IC-060）
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

rootProject.name = "ic-chain"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
