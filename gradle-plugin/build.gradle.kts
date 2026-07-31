plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.projectmapk"

version = "1.0-SNAPSHOT"

kotlin { jvmToolchain(17) }

dependencies {
    compileOnly(libs.kotlin.gradle.plugin.api)
    // DSL の LabelCase と runtime-api の宣言の一致検証（LabelCaseTest）のため、依存はテストにのみ持つ
    testImplementation(project(":sealed-class-enumizer-runtime-api"))
    testImplementation(libs.kotlin.test)
}

tasks.test { useJUnitPlatform() }

gradlePlugin {
    plugins {
        create("sealedClassEnumizer") {
            id = "io.github.projectmapk.sealed-class-enumizer"
            implementationClass =
                "io.github.projectmapk.sealedClassEnumizer.gradle.SealedClassEnumizerGradlePlugin"
        }
    }
}
