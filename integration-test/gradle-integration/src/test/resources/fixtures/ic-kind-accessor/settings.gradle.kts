// TestKit フィクスチャ: 参照不能 kind 用 IR-only アクセサの IC 決定性検証
// （clean / incremental / from-cache のバイト一致。%% プレースホルダは TestKitHarness が置換する）
pluginManagement {
    includeBuild("%%PARENT_BUILD%%")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "ic-kind-accessor"

includeBuild("%%PARENT_BUILD%%")

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
