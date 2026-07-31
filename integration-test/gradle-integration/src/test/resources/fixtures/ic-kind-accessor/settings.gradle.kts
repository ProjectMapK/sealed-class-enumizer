// TestKit フィクスチャ: 参照不能 kind 用 IR-only アクセサの IC 決定性検証
// （clean / incremental / from-cache のバイト一致。%% プレースホルダは TestKitHarness が置換する）
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

rootProject.name = "ic-kind-accessor"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
