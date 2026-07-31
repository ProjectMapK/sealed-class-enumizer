import com.ncorti.ktfmt.gradle.KtfmtPlugin

// ルートプロジェクトは集約のみを担う。各モジュールの構成は各自の build.gradle.kts が持つ。
// integration-test は独立した composite build であり、ここには含めない（docs/test/テスト戦略.md）。
//
// plugins ブロックは適用せず宣言のみ行う（apply false）。サブプロジェクト毎に異なるプラグイン集合を
// 要求するとクラスローダが分裂し、KGP の共有 build service（KotlinNativeBundleBuildService）が
// 型不一致になって IDE sync（prepareKotlinIdeaImport → commonizeNativeDistribution）が失敗するため、
// 全プラグインをルートのクラスローダへ一度だけロードして共有させる。
// ktfmt はモジュール構成ではなくビルド全体の共通規約であり、スタイル定義を 1 か所に保つため
// ルートで適用して allprojects へ配る（ルート自身の build.gradle.kts / settings.gradle.kts も対象になる）。
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.autoservice) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
    alias(libs.plugins.gradle.plugin.publish) apply false
    // ルートは API ドキュメントの集約点として適用する（下の dokka 依存）
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktfmt)
}

// 公開 group と版の単一情報源。版は <KotlinVersion>-<自版> 形式（docs/概要.md §7 のマイナー毎分割の実現）で、
// Kotlin 版は version catalog・自版は gradle.properties の enumizerVersion が正。
// 開発中の自版は -SNAPSHOT を維持する（ローカル Maven の上書き公開・非キャッシュという
// integration-test の local-repo 経路の前提のため）
val enumizerFullVersion =
    "${libs.versions.kotlin.get()}-${providers.gradleProperty("enumizerVersion").get()}"

allprojects {
    group = "io.github.projectmapk"
    version = enumizerFullVersion
}

// gradle.properties の kotlin.code.style=official に合わせ、ktfmt も Kotlin 公式スタイル
// （ブロック・継続ともインデント 4、末尾カンマ付与）で揃える
allprojects {
    apply<KtfmtPlugin>()

    ktfmt { kotlinLangStyle() }
}

// API ドキュメント（GitHub Pages 掲載用）の集約。対象は公開 API を持つ 2 モジュールで、
// compiler-plugin は内部実装のため含めない。集約出力は build/dokka/html
dependencies {
    dokka(project(":sealed-class-enumizer-runtime-api"))
    dokka(project(":sealed-class-enumizer-gradle-plugin"))
}

// integration-test の TestKit フィクスチャ向けに、3 モジュールのローカル Maven 公開を集約する
// （docs/test/フィクスチャ構成.md §4 の local-repo 経路）。ローカル Maven を使うのは
// 非タイムスタンプの SNAPSHOT が上書き公開され、Gradle が成果物をキャッシュしないため
tasks.register("publishAllToMavenLocal") {
    dependsOn(subprojects.map { "${it.path}:publishToMavenLocal" })
}
