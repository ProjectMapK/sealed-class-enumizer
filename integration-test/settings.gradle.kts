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

// repositoriesMode は親ビルドと同じく既定（PREFER_PROJECT）とする。KGP の JS / Wasm ツールチェーン
// （Node.js / Yarn / Binaryen 配布物）はプロジェクトレベルの ivy リポジトリを動的に追加するため、
// PREFER_SETTINGS では org.nodejs:node 等が解決できず mpp-* の JS / Wasm ターゲットがビルド不能になる
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "enumize-integration-test"

includeBuild("..")

// docs/テストケース管理.md のモジュール一覧（8 モジュール）
include(
    ":producer-jvm",
    ":consumer-pure-jvm",
    ":consumer-plugin-jvm",
    ":downstream-subtype-jvm",
    ":java-consumer",
    ":mpp-producer",
    ":mpp-consumer",
    ":gradle-integration",
)
