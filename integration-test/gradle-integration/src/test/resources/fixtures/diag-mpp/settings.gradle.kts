// diag-mpp フィクスチャ（docs/test/ケース04-診断.md DIA-06〜10）:
// KMP jvm 単一ターゲットの fail モジュール（expect/actual 負値・cross-source-set）と
// ok モジュール（platform 専用階層の非発火）を 1 フィクスチャへ束ねる
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("multiplatform") version "2.4.20-Beta1"
        id("org.wrongwrong.sealed-class-enumizer") version "1.0-SNAPSHOT"
    }
}

rootProject.name = "diag-mpp"

include(":fail", ":ok")
