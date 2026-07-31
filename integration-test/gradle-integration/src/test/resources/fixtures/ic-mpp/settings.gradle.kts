// TestKit フィクスチャ: MPP（jvm + js / wasmJs の klib）での末端追加ラウンド
// docs/test/ケース06-ビルド動態.md BLD-42（V8 の klib IC）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.0"
        kotlin("multiplatform") version "2.4.0"
        id("io.github.projectmapk.sealed-class-enumizer") version "1.0-SNAPSHOT"
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
