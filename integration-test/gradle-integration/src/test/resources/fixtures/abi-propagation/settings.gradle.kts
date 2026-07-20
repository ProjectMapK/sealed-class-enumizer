// TestKit フィクスチャ: 跨モジュール ABI 伝播（設計00 §5.3 #11・docs/テストケース管理.md TC-XM-013 系）
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

rootProject.name = "abi-propagation"

includeBuild("%%PARENT_BUILD%%")

include(
    ":producer",
    ":consumer",
)

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
