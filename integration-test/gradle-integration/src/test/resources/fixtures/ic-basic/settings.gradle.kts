// TestKit フィクスチャ: プラグイン一式をローカル Maven から解決する
// （docs/test/フィクスチャ構成.md §4。%% プレースホルダは TestKitHarness が置換する）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
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

rootProject.name = "ic-basic"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
