import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin { jvmToolchain(17) }

dependencies {
    // 拡張点（KotlinMavenPluginExtension / PluginOption）と Maven のモデル型は実行時に
    // kotlin-maven-plugin の realm 側が供給するため、コンパイル時にのみ参照する
    compileOnly(libs.kotlin.maven.plugin)
    compileOnly(libs.maven.core)
    // コンパイラプラグイン本体は POM の依存として配る。kotlin-maven-plugin は自身の <dependencies> を
    // 推移解決してコンパイラのプラグインクラスパスへ載せるため、利用側は本成果物 1 つの宣言で足りる
    api(project(":sealed-class-enumizer-compiler-plugin"))
    testImplementation(libs.kotlin.maven.plugin)
    testImplementation(libs.maven.core)
    // 受け付ける LabelCase と runtime-api の宣言の一致検証（LabelCaseTest）のため、依存はテストにのみ持つ
    testImplementation(project(":sealed-class-enumizer-runtime-api"))
    testImplementation(libs.kotlin.test)
}

tasks.test { useJUnitPlatform() }

// Maven Central 公開設定（POM の共通値はルート、モジュール別値は本モジュールの gradle.properties）。
// 本成果物は Mojo ではなく kotlin-maven-plugin の拡張であり、plugin descriptor は持たない
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    configure(KotlinJvm(javadocJar = JavadocJar.Empty()))
}

// SealedClassEnumizerCoordinates はビルドの値（version / Kotlin 版 / コンパイラプラグイン ID）から生成し、
// ソース側での手動同期を持たない（gradle-plugin と同じ方式）
val coordinatesDir = layout.buildDirectory.dir("generated/sources/coordinates/kotlin")

val generateCoordinates =
    tasks.register("generateCoordinates") {
        val version = project.version.toString()
        val kotlinVersion = libs.versions.kotlin.get()
        val compilerPluginId = providers.gradleProperty("compilerPluginId").get()
        val outputDir = coordinatesDir
        inputs.property("version", version)
        inputs.property("kotlinVersion", kotlinVersion)
        inputs.property("compilerPluginId", compilerPluginId)
        outputs.dir(outputDir)
        doLast {
            val file =
                outputDir
                    .get()
                    .file(
                        "io/github/projectmapk/sealedClassEnumizer/maven/SealedClassEnumizerCoordinates.kt"
                    )
                    .asFile
            file.parentFile.mkdirs()
            file.writeText(
                """
                |// generateCoordinates タスクが生成するファイル（手動編集しない）。
                |// 値の正はビルド側（version / Kotlin 版 / コンパイラプラグイン ID）にある
                |package io.github.projectmapk.sealedClassEnumizer.maven
                |
                |internal object SealedClassEnumizerCoordinates {
                |    const val VERSION: String = "$version"
                |    const val KOTLIN_VERSION: String = "$kotlinVersion"
                |    const val COMPILER_PLUGIN_ID: String = "$compilerPluginId"
                |}
                |"""
                    .trimMargin()
            )
        }
    }

kotlin.sourceSets.getByName("main").kotlin.srcDir(generateCoordinates)
