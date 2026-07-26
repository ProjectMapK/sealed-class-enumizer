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

rootProject.name = "ic-scope-tracking"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
