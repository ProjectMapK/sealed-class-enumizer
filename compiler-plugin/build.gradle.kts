plugins {
    alias(libs.plugins.kotlin.jvm)
    // @AutoService から META-INF/services を生成する（手書きのサービス登録ファイルは置かない）
    alias(libs.plugins.autoservice)
    `maven-publish`
}

group = "org.wrongwrong"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)
}

// TestKit フィクスチャがプラグイン一式を座標で解決するための publication
// （docs/テストケース管理.md Gradle TestKit 方針の local-repo 経路）
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
