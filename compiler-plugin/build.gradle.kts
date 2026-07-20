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

// 診断テスト（コンパイラを直接起動する単体テスト）が -classpath へ渡す runtime-api の JVM 変種
val runtimeApiForTests: Configuration by configurations.creating {
    isCanBeConsumed = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)
    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.kotlin.test)
    runtimeApiForTests(project(":runtime-api"))
}

tasks.test {
    useJUnitPlatform()
    val pluginJar = tasks.jar.flatMap { it.archiveFile }
    inputs.file(pluginJar)
    inputs.files(runtimeApiForTests)
    doFirst {
        systemProperty("enumize.test.pluginJar", pluginJar.get().asFile.absolutePath)
        systemProperty("enumize.test.runtimeClasspath", runtimeApiForTests.asPath)
    }
}
