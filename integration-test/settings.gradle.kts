// integration-test は「その内側で完結する独立した Gradle ビルド」であり、親ビルド
// （runtime-api / compiler-plugin / gradle-plugin）を composite 参照する（docs/テストケース管理.md）。
// 親ビルドには include しない（プラグインの自己適用・依存循環の回避・重いテストの分離のため）。
pluginManagement {
    includeBuild("..")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
        kotlin("multiplatform") version "2.4.20-Beta1"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "enumize-integration-test"

includeBuild("..")

// docs/テストケース管理.md のモジュール一覧のうち、実装済みのものから順に include する
include(
    ":producer-jvm",
)
