// runtime-api 依存露出フィクスチャ（docs/テストケース管理.md TC-XM-006 / TC-XM-056）:
// producer が runtime-api を api 公開すれば未適用 consumer が推移取得できること（正）と、
// implementation で隠した縮退では supertype 解決に失敗すること（負）を 1 対で検証する
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
        id("org.wrongwrong.sealed-class-enumizer") version "1.0-SNAPSHOT"
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
