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
        kotlin("multiplatform") version "%%KOTLIN_VERSION%%"
        id("io.github.projectmapk.sealed-class-enumizer") version "%%ENUMIZER_VERSION%%"
    }
}

rootProject.name = "diag-mpp"

include(":fail", ":ok")
