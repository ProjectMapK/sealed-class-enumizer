// TestKit フィクスチャ: 多段中間 sealed チェーン（docs/テストケース管理.md TC-IC-060）
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

rootProject.name = "ic-chain"

includeBuild("%%PARENT_BUILD%%")

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
