// diag-ok フィクスチャ（docs/test/ケース04-診断.md の near-miss 非発火・ES 警告・raw 追跡表記を
// 1 回の成功ビルドへ束ねる。行位置はアサートの一部 = docs/test/フィクスチャ構成.md §5）
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

rootProject.name = "diag-ok"
