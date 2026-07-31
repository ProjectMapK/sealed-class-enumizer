// TestKit フィクスチャ: MPP（jvm + js / wasmJs の klib）での末端追加ラウンド
// docs/test/ケース06-ビルド動態.md BLD-42（V8 の klib IC）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "%%KOTLIN_VERSION%%"
        kotlin("multiplatform") version "%%KOTLIN_VERSION%%"
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

rootProject.name = "ic-mpp"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
