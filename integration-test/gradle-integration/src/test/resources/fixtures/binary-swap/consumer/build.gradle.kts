// 消費側（プラグイン未適用）。コンパイルは常に v1 jar、実行時のみ v1 / v2 を切り替える
plugins {
    kotlin("jvm")
}

val v1Jar = rootDir.resolve("producer-v1/build/libs/producer-v1.jar")
val v2Jar = rootDir.resolve("producer-v2/build/libs/producer-v2.jar")

dependencies {
    // コンパイルは常に v1（2 末端）に対して行う（テストが先に :producer-v1:jar を組み立てる）
    implementation(files(v1Jar))
    // 暫定: 未適用消費側は runtime-api の明示宣言が必要（docs/修正方針案.md #1）
    implementation("org.wrongwrong:runtime-api:1.0-SNAPSHOT")
}

// 実行時クラスパスから producer jar を除いた共通部分（main 出力 + runtime-api + stdlib）
fun runtimeWithoutProducer() = sourceSets.main.get().runtimeClasspath.filter {
    !it.name.startsWith("producer-")
}

tasks.register<JavaExec>("runV1") {
    classpath = runtimeWithoutProducer() + files(v1Jar)
    mainClass.set("org.wrongwrong.swapuse.MainKt")
}

tasks.register<JavaExec>("runV2") {
    // コンパイル時 v1・実行時 v2 の組合せ（entries は実行時に存在する版の集合を返す）
    classpath = runtimeWithoutProducer() + files(v2Jar)
    mainClass.set("org.wrongwrong.swapuse.MainKt")
}
