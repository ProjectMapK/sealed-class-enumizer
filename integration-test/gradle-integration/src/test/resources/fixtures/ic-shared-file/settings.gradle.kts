// TestKit フィクスチャ: 同一ファイルに 2 階層を同居させる境界（docs/テストケース管理.md TC-IC-062）
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

rootProject.name = "ic-shared-file"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
