import com.vanniktech.maven.publish.GradlePublishPlugin

plugins {
    alias(libs.plugins.kotlin.jvm)
    // java-gradle-plugin と maven-publish を適用し、Plugin Portal への公開（publishPlugins）と
    // sources / javadoc jar の構成も担う
    alias(libs.plugins.gradle.plugin.publish)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin { jvmToolchain(17) }

dependencies {
    compileOnly(libs.kotlin.gradle.plugin.api)
    // DSL の LabelCase と runtime-api の宣言の一致検証（LabelCaseTest）のため、依存はテストにのみ持つ
    testImplementation(project(":sealed-class-enumizer-runtime-api"))
    testImplementation(libs.kotlin.test)
}

tasks.test { useJUnitPlatform() }

// plugin id の正定義。compiler-plugin の EnumizeCommandLineProcessor.PLUGIN_ID と一致していなければならない
val pluginId = "io.github.projectmapk.sealed-class-enumizer"

gradlePlugin {
    website = providers.gradleProperty("POM_URL")
    vcsUrl = providers.gradleProperty("POM_URL")
    plugins {
        create("sealedClassEnumizer") {
            id = pluginId
            implementationClass =
                "io.github.projectmapk.sealedClassEnumizer.gradle.SealedClassEnumizerGradlePlugin"
            displayName = "Sealed Class Enumizer"
            description =
                "Applies the sealed-class-enumizer Kotlin compiler plugin and adds its runtime API dependency."
            tags = listOf("kotlin", "kotlin-compiler-plugin", "sealed-class", "enum")
        }
    }
}

// Maven Central 公開設定（POM の共通値はルート、モジュール別値は本モジュールの gradle.properties。
// plugin marker の publication も含めて署名・公開の対象になる）
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    configure(GradlePublishPlugin())
}

// SealedClassEnumizerCoordinates はビルドの値（group / version / 兄弟モジュールの公開名 / plugin id）から
// 生成し、ソース側での手動同期を持たない。公開名はプロジェクト名の参照で得るため、改名時はここが即座に失敗する
val coordinatesDir = layout.buildDirectory.dir("generated/sources/coordinates/kotlin")

val generateCoordinates =
    tasks.register("generateCoordinates") {
        val group = project.group.toString()
        val version = project.version.toString()
        val compilerPluginArtifact =
            rootProject.project(":sealed-class-enumizer-compiler-plugin").name
        val runtimeApiArtifact = rootProject.project(":sealed-class-enumizer-runtime-api").name
        val id = pluginId
        val outputDir = coordinatesDir
        inputs.property("group", group)
        inputs.property("version", version)
        inputs.property("compilerPluginArtifact", compilerPluginArtifact)
        inputs.property("runtimeApiArtifact", runtimeApiArtifact)
        inputs.property("pluginId", id)
        outputs.dir(outputDir)
        doLast {
            val file =
                outputDir
                    .get()
                    .file(
                        "io/github/projectmapk/sealedClassEnumizer/gradle/SealedClassEnumizerCoordinates.kt"
                    )
                    .asFile
            file.parentFile.mkdirs()
            file.writeText(
                """
                |// generateCoordinates タスクが生成するファイル（手動編集しない）。
                |// 値の正はビルド側（group / version / 兄弟モジュールの公開名 / plugin id）にある
                |package io.github.projectmapk.sealedClassEnumizer.gradle
                |
                |object SealedClassEnumizerCoordinates {
                |    const val GROUP: String = "$group"
                |    const val VERSION: String = "$version"
                |    const val COMPILER_PLUGIN_ARTIFACT: String = "$compilerPluginArtifact"
                |    const val RUNTIME_API_ARTIFACT: String = "$runtimeApiArtifact"
                |    const val COMPILER_PLUGIN_ID: String = "$id"
                |}
                |"""
                    .trimMargin()
            )
        }
    }

kotlin.sourceSets.getByName("main").kotlin.srcDir(generateCoordinates)
