// sweep-typealias-impl フィクスチャ（階層内の手動実装を typealias 経由で書いた場合に、生成 Enumish の
// 継承者一覧へ反映されるかの観測。明示形を同一ビルドの対照として並べる）
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "%%KOTLIN_VERSION%%"
        kotlin("multiplatform") version "%%KOTLIN_VERSION%%"
        id("io.github.projectmapk.sealed-class-enumizer") version "%%ENUMIZER_VERSION%%"
    }
}

rootProject.name = "sweep-typealias-impl"
