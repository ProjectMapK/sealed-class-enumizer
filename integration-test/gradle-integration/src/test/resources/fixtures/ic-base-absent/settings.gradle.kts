// TestKit フィクスチャ: 多ファイル sealed × プラグイン生成 × IC の連続編集と基底不在ラウンド
// docs/コンパイラプラグイン設計00.md §5.4・§9-4・docs/テストケース管理.md TC-IC-039/040
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

rootProject.name = "ic-base-absent"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
