// TestKit フィクスチャ: 同一ファイルに 2 階層を同居させる境界（docs/test/ケース06-ビルド動態.md BLD-32）
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

rootProject.name = "ic-shared-file"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
