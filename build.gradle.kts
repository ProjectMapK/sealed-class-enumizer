// ルートプロジェクトは集約のみを担う。各モジュールの構成は各自の build.gradle.kts が持つ。
// integration-test は独立した composite build であり、ここには含めない（docs/テストケース管理.md）。
//
// plugins ブロックは適用せず宣言のみ行う（apply false）。サブプロジェクト毎に異なるプラグイン集合を
// 要求するとクラスローダが分裂し、KGP の共有 build service（KotlinNativeBundleBuildService）が
// 型不一致になって IDE sync（prepareKotlinIdeaImport → commonizeNativeDistribution）が失敗するため、
// 全プラグインをルートのクラスローダへ一度だけロードして共有させる。
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.autoservice) apply false
}

// integration-test の TestKit フィクスチャ向けに、3 モジュールのローカル Maven 公開を集約する
// （docs/テストケース管理.md Gradle TestKit 方針の local-repo 経路）。ローカル Maven を使うのは
// 非タイムスタンプの SNAPSHOT が上書き公開され、Gradle が成果物をキャッシュしないため
tasks.register("publishAllToMavenLocal") {
    dependsOn(subprojects.map { "${it.path}:publishToMavenLocal" })
}
