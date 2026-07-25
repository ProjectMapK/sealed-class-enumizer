// TestKit フィクスチャ: MPP（jvm + js / wasmJs の klib）での末端追加ラウンド
// docs/テストケース管理.md TC-IC-069（V8 の klib IC）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
        kotlin("multiplatform") version "2.4.20-Beta1"
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

rootProject.name = "ic-mpp"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
