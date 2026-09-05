import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    alias(libs.plugins.kotlin.jvm)
    // @AutoService から META-INF/services を生成する（手書きのサービス登録ファイルは置かない）
    alias(libs.plugins.autoservice)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin { jvmToolchain(17) }

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)
    // プラグイン本体は runtime-api をロードせず、名前定数はリテラルで持つ（公式・著名プラグインと同じ方針）。
    // 定数と runtime-api の宣言の一致は EnumizeNamesTest が担保するため、依存はテストにのみ持つ
    testImplementation(project(":sealed-class-enumizer-runtime-api"))
    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.kotlin.test)
    // 値引数名の取得（KCallable.parameters）に必要
    testImplementation(libs.kotlin.reflect)
}

// Maven Central 公開設定（POM の共通値はルート、モジュール別値は本モジュールの gradle.properties）。
// TestKit フィクスチャの local-repo 経路（docs/test/フィクスチャ構成.md §5）も
// この publication の publishToMavenLocal を使う。javadoc jar は空で Central の要件を満たす
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    configure(KotlinJvm(javadocJar = JavadocJar.Empty()))
}

// 診断・跨 module 負値のテストは、テスト JVM 内で CLI コンパイラを直接駆動してフィクスチャを
// コンパイルする（docs/test/テスト戦略.md §4）。コンパイラプラグインは配布物と同じ jar を
// プラグインクラスパスへ渡して読み込ませるため、登録経路は実ビルドと同一になる。
// フィクスチャは診断の行番号を検証する入力であり、ソースセットへは入れない（整形・コンパイルの対象外）
tasks.test {
    useJUnitPlatform()
    val fixtures = layout.projectDirectory.dir("src/test/fixtures")
    val workRoot = layout.buildDirectory.dir("test-compilations")
    inputs
        .files(tasks.jar.flatMap { it.archiveFile })
        .withPropertyName("compilerPluginJar")
        .withNormalizer(ClasspathNormalizer::class)
    inputs.dir(fixtures).withPropertyName("fixtures").withPathSensitivity(PathSensitivity.RELATIVE)
    systemProperty("enumizer.pluginJar", tasks.jar.get().archiveFile.get().asFile.absolutePath)
    systemProperty("enumizer.fixtureRoot", fixtures.asFile.absolutePath)
    systemProperty("enumizer.compileWorkRoot", workRoot.get().asFile.absolutePath)
}
