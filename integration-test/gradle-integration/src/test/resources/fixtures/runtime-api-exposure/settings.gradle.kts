// runtime-api 依存露出フィクスチャ（docs/test/ケース06-ビルド動態.md BLD-41）:
// producer が runtime-api を api 公開すれば未適用 consumer が推移取得できること（正）と、
// implementation で隠した縮退では supertype 解決に失敗すること（負）を 1 対で検証する
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.0"
        id("io.github.projectmapk.sealed-class-enumizer") version "1.0-SNAPSHOT"
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "runtime-api-exposure"

include(
    ":producer",
    ":consumer",
)
