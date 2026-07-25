// sweep-typealias-leaf フィクスチャ（末端 object が生成 Enumish を typealias 経由で冗長宣言する形の観測）
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

rootProject.name = "sweep-typealias-leaf"
