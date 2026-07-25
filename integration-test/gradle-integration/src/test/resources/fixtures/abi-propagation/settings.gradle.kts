// TestKit フィクスチャ: 跨モジュール ABI 伝播（設計00 §5.3 #11・docs/テストケース管理.md TC-XM-013 系）
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
