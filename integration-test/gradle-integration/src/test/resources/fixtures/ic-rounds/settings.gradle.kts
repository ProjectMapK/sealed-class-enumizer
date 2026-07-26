// TestKit フィクスチャ: 多ファイル sealed × プラグイン生成 × IC の連続編集・基底不在ラウンド・
// 多段中間チェーン・空⇔非空遷移（docs/コンパイラプラグイン設計00.md §5・§9-4・
// docs/test/ケース06-ビルド動態.md §3）
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

rootProject.name = "ic-rounds"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
