// sweep-typealias-impl フィクスチャ（階層内の手動実装を typealias 経由で書いた場合に、生成 Enumish の
// 継承者一覧へ反映されるかの観測。明示形を同一ビルドの対照として並べる）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.0"
        kotlin("multiplatform") version "2.4.0"
        id("io.github.projectmapk.sealed-class-enumizer") version "1.0-SNAPSHOT"
    }
}

rootProject.name = "sweep-typealias-impl"
