plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "org.wrongwrong"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)
}
