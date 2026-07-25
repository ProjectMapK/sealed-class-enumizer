// sweep-typealias-head フィクスチャ（手動 Enumized<生成 Enumish> 自体を typealias で書いた基底の観測）
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

rootProject.name = "sweep-typealias-head"
