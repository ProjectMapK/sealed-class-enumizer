// TestKit フィクスチャ: 基底より広い末端の companion 可視性トグル
// （規則 1 ⇔ 規則 3 の発火・解除 = docs/test/ケース06-ビルド動態.md BLD-18）
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

rootProject.name = "ic-denotable"

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
