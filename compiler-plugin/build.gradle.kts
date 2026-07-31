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

tasks.test { useJUnitPlatform() }

// Maven Central 公開設定（POM の共通値はルート、モジュール別値は本モジュールの gradle.properties）。
// TestKit フィクスチャの local-repo 経路（docs/test/フィクスチャ構成.md §4）も
// この publication の publishToMavenLocal を使う。javadoc jar は空で Central の要件を満たす
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    configure(KotlinJvm(javadocJar = JavadocJar.Empty()))
}
