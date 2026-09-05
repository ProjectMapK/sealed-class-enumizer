// TestKit フィクスチャ: プラグイン一式をローカル Maven から解決する
// （docs/test/フィクスチャ構成.md §5。%% プレースホルダは TestKitHarness が置換する）
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

rootProject.name = "ic-basic"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
