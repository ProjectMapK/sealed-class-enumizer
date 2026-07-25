// TestKit フィクスチャ: プラグイン一式をローカル Maven から解決する
// （docs/テストケース管理.md Gradle TestKit 方針。%% プレースホルダは TestKitHarness が置換する）
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

rootProject.name = "ic-basic"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
