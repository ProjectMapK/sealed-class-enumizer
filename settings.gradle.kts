plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

rootProject.name = "sealed-class-enumizer"

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories { mavenCentral() }
    // マイナー横断ビルド用の差し替え口: -PkotlinVersionOverride=<版> で catalog の Kotlin 版を上書きする
    // （宣言の正は gradle/libs.versions.toml。ここでの version() は自動取込みの値より優先される）
    versionCatalogs {
        create("libs") {
            val kotlinOverride = providers.gradleProperty("kotlinVersionOverride")
            if (kotlinOverride.isPresent) {
                version("kotlin", kotlinOverride.get())
            }
        }
    }
}

include(":runtime-api", ":compiler-plugin", ":gradle-plugin")

// 公開 artifactId は「sealed-class-enumizer-」接頭辞付きとする（group 直下に汎用名を置かない）。
// artifactId の源泉であるプロジェクト名ごと揃えることで、KMP のターゲット別 publication の派生名と、
// integration-test からの composite 自動置換（group:プロジェクト名での対応付け）を追加設定なしに保つ。
// ディレクトリ名は従来の短い名前のままとする
project(":runtime-api").name = "sealed-class-enumizer-runtime-api"

project(":compiler-plugin").name = "sealed-class-enumizer-compiler-plugin"

project(":gradle-plugin").name = "sealed-class-enumizer-gradle-plugin"
