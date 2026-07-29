// sweep-typealias-samefile-head フィクスチャ（手動 Enumized への typealias を、エイリアスが階層より
// 先に解決されない配置＝同一ファイルに置いた形の観測。先に処理される配置は sweep-typealias-head）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
        kotlin("multiplatform") version "2.4.20-Beta1"
        id("io.github.projectmapk.sealed-class-enumizer") version "1.0-SNAPSHOT"
    }
}

rootProject.name = "sweep-typealias-samefile-head"
