// TestKit フィクスチャ: 跨モジュール ABI 伝播（docs/コンパイラプラグイン設計00.md §5.3 #11・docs/test/ケース06-ビルド動態.md §4）
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

rootProject.name = "abi-propagation"

include(
    ":producer",
    ":consumer",
)

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
