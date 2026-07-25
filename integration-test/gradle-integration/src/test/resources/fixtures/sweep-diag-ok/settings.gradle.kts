// sweep-diag-ok フィクスチャ（docs/テストケース管理.md 残ケース掃討・非発火 near-miss の補完）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
        kotlin("multiplatform") version "2.4.20-Beta1"
        id("org.wrongwrong.sealed-class-enumizer") version "1.0-SNAPSHOT"
    }
}

rootProject.name = "sweep-diag-ok"

