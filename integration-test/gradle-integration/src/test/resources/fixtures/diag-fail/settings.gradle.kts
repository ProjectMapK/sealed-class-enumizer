// diag-fail フィクスチャ（docs/test/ケース04-診断.md の発火系・言語委譲を 1 buildAndFail へ束ねる）。
// 各宣言は互いに独立（1 宣言 1 診断）で、行位置はアサートの一部（docs/test/フィクスチャ構成.md §5）
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

rootProject.name = "diag-fail"
