plugins {
    alias(libs.plugins.kotlin.jvm)
    // @AutoService から META-INF/services を生成する（手書きのサービス登録ファイルは置かない）
    alias(libs.plugins.autoservice)
    `maven-publish`
}

group = "org.wrongwrong"

version = "1.0-SNAPSHOT"

kotlin { jvmToolchain(17) }

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)
    // プラグイン本体は runtime-api をロードせず、名前定数はリテラルで持つ（公式・著名プラグインと同じ方針）。
    // 定数と runtime-api の宣言の一致は EnumizeNamesTest が担保する
    // （docs/コンパイラプラグイン設計00.md §9）ため、依存はテストにのみ持つ
    testImplementation(project(":runtime-api"))
    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.kotlin.test)
    // 値引数名の取得（KCallable.parameters）に必要
    testImplementation(libs.kotlin.reflect)
}

tasks.test { useJUnitPlatform() }

// TestKit フィクスチャがプラグイン一式を座標で解決するための publication
// （docs/テストケース管理.md Gradle TestKit 方針の local-repo 経路）
publishing { publications { create<MavenPublication>("maven") { from(components["java"]) } } }
