plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.projectmapk"

version = "1.0-SNAPSHOT"

kotlin { jvmToolchain(17) }

dependencies { compileOnly(libs.kotlin.gradle.plugin.api) }

gradlePlugin {
    plugins {
        create("sealedClassEnumizer") {
            id = "io.github.projectmapk.sealed-class-enumizer"
            implementationClass =
                "io.github.projectmapk.sealedClassEnumizer.gradle.SealedClassEnumizerGradlePlugin"
        }
    }
}
