// TestKit フィクスチャ: 同一ファイルに 2 階層を同居させる境界（docs/テストケース管理.md TC-IC-062）
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

rootProject.name = "ic-shared-file"

includeBuild("%%PARENT_BUILD%%")

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
