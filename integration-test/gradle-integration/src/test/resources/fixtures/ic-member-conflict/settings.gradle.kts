// ic-member-conflict フィクスチャ（docs/test/ケース06-ビルド動態.md BLD-47 final 継承トグル）
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

rootProject.name = "ic-member-conflict"
