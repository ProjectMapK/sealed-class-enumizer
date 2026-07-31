// 消費側（プラグイン未適用）。コンパイルは常に v1 jar、実行時のみ v1 / v2 を切り替える
plugins {
    kotlin("jvm")
}

val v1Jar = rootDir.resolve("producer-v1/build/libs/producer-v1.jar")
val v2Jar = rootDir.resolve("producer-v2/build/libs/producer-v2.jar")

dependencies {
    // コンパイルは常に v1（2 末端）に対して行う（テストが先に :producer-v1:jar を組み立てる）
    implementation(files(v1Jar))
    // producer を raw jar（files）で参照するため推移的メタデータが無く、runtime-api の明示宣言が必要。
    // これは gradle-plugin の api 自動公開とは独立の恒久要件（生成 API の supertype 解決に要る）
    implementation("io.github.projectmapk:sealed-class-enumizer-runtime-api:%%ENUMIZER_VERSION%%")
}

// 実行時クラスパスから producer jar を除いた共通部分（main 出力 + runtime-api + stdlib）
fun runtimeWithoutProducer() = sourceSets.main.get().runtimeClasspath.filter {
    !it.name.startsWith("producer-")
}

tasks.register<JavaExec>("runV1") {
    classpath = runtimeWithoutProducer() + files(v1Jar)
    mainClass.set("io.github.projectmapk.swapuse.MainKt")
}

tasks.register<JavaExec>("runV2") {
    // コンパイル時 v1・実行時 v2 の組合せ（entries は実行時に存在する版の集合を返す）
    classpath = runtimeWithoutProducer() + files(v2Jar)
    mainClass.set("io.github.projectmapk.swapuse.MainKt")
}
