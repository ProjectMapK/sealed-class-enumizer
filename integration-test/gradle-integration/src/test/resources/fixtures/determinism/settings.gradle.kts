// TestKit フィクスチャ: 決定性検証（clean / incremental / from-cache / relocated のバイト一致）
// docs/コンパイラプラグイン設計00.md §9・docs/コンパイラプラグイン設計02.md §6・docs/test/ケース06-ビルド動態.md §1
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.0"
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

rootProject.name = "determinism"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
