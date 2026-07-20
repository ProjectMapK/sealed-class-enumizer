// TestKit フィクスチャ: 決定性検証（clean / incremental / from-cache / relocated のバイト一致）
// docs/コンパイラプラグイン設計00.md §9・設計02 §6・docs/テストケース管理.md Gradle TestKit 方針
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

rootProject.name = "determinism"

includeBuild("%%PARENT_BUILD%%")

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
