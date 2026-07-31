// TestKit フィクスチャ: 候補判定のスコープ解決を変える跨ファイル編集 × IC
// （typealias の付け替え・宣言ファイル張り替え・同一 pkg 囮の削除。
// docs/コンパイラプラグイン設計00.md §10 V7・docs/test/ケース06-ビルド動態.md BLD-45）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "%%KOTLIN_VERSION%%"
        id("io.github.projectmapk.sealed-class-enumizer") version "%%ENUMIZER_VERSION%%"
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "ic-scope-tracking"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
