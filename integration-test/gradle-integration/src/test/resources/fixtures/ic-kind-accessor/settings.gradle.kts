// TestKit フィクスチャ: 参照不能 kind 用 IR-only アクセサの IC 決定性検証
// （clean / incremental / from-cache のバイト一致。%% プレースホルダは TestKitHarness が置換する）
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

rootProject.name = "ic-kind-accessor"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
