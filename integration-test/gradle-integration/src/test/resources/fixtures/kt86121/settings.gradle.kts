// TestKit フィクスチャ: KT-86121 型（多ファイル sealed × プラグイン生成 × IC の連続編集）
// docs/コンパイラプラグイン設計00.md §5.4・§9-4・docs/テストケース管理.md TC-IC-039/040
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

rootProject.name = "kt86121"

includeBuild("%%PARENT_BUILD%%")

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
