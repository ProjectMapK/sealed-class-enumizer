plugins {
    alias(libs.plugins.kotlin.jvm)
    // @AutoService から META-INF/services を生成する（手書きのサービス登録ファイルは置かない）
    alias(libs.plugins.autoservice)
}

group = "org.wrongwrong"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)
}
