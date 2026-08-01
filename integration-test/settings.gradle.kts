// integration-test は「その内側で完結する独立した Gradle ビルド」であり、親ビルド
// （runtime-api / compiler-plugin / gradle-plugin）を composite 参照する（docs/test/テスト戦略.md）。
// 親ビルドには include しない（プラグインの自己適用・依存循環の回避・重いテストの分離のため）。
pluginManagement {
    // included build の名前は既定でチェックアウト先のディレクトリ名になる。git worktree ではその名前が
    // 変わり、名前で親ビルドを引く参照（:gradle-integration:test の publishAllToMavenLocal 依存）が
    // 解決できなくなるため明示する。名前を持てるのは最初の登録であるこちらのみで、後段の
    // includeBuild("..") では既に登録済みのため指定しても効かない
    includeBuild("..") { name = "sealed-class-enumizer" }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

// repositoriesMode は親ビルドと同じく既定（PREFER_PROJECT）とする。KGP の JS / Wasm ツールチェーン
// （Node.js / Yarn / Binaryen 配布物）はプロジェクトレベルの ivy リポジトリを動的に追加するため、
// PREFER_SETTINGS では org.nodejs:node 等が解決できず mpp-* の JS / Wasm ターゲットがビルド不能になる
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories { mavenCentral() }
    // KGP のバージョンは親ビルドの version catalog を唯一の源とする。独立したビルドには catalog が
    // 自動共有されないため、親の TOML を明示的に読み込む。参照は build.gradle.kts の alias(...) が行う
    // （catalog は pluginManagement からは参照できないため、バージョン宣言は settings ではなく
    // ルートの plugins ブロックに置く）
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
            // マイナー横断ビルド用の差し替え口（親ビルドと同じ -PkotlinVersionOverride を受ける。
            // フィクスチャへは TestKitHarness の %%KOTLIN_VERSION%% 置換経由で伝播する）
            val kotlinOverride = providers.gradleProperty("kotlinVersionOverride")
            if (kotlinOverride.isPresent) {
                version("kotlin", kotlinOverride.get())
            }
        }
    }
}

rootProject.name = "enumize-integration-test"

includeBuild("..")

// docs/test/テスト戦略.md のモジュール一覧（9 モジュール）
include(
    ":producer-jvm",
    ":consumer-pure-jvm",
    ":consumer-plugin-jvm",
    ":downstream-subtype-jvm",
    ":java-consumer",
    ":mpp-producer",
    ":mpp-consumer",
    ":gradle-integration",
    ":maven-integration",
)
