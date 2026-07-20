plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
}

group = "org.wrongwrong"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin.api)
}

gradlePlugin {
    plugins {
        create("sealedClassEnumizer") {
            id = "org.wrongwrong.sealed-class-enumizer"
            implementationClass = "org.wrongwrong.sealedClassEnumizer.gradle.SealedClassEnumizerGradlePlugin"
        }
    }
}
