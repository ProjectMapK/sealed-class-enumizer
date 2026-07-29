// 消費側その 2（プラグイン未適用）。コンパイルは常に v2 jar、実行時のみ v1 へ差し替える
// （削除方向 = docs/test/ケース06-ビルド動態.md BLD-40）
plugins {
    kotlin("jvm")
}

val v1Jar = rootDir.resolve("producer-v1/build/libs/producer-v1.jar")
val v2Jar = rootDir.resolve("producer-v2/build/libs/producer-v2.jar")

dependencies {
    // コンパイルは常に v2（3 末端）に対して行う（テストが先に :producer-v2:jar を組み立てる）
    implementation(files(v2Jar))
    // raw jar（files）参照のため推移的メタデータが無く、runtime-api の明示宣言が必要
    implementation("io.github.projectmapk:runtime-api:1.0-SNAPSHOT")
}

// 実行時クラスパスから producer jar を除いた共通部分（main 出力 + runtime-api + stdlib）
fun runtimeWithoutProducer() = sourceSets.main.get().runtimeClasspath.filter {
    !it.name.startsWith("producer-")
}

tasks.register<JavaExec>("runV1") {
    // コンパイル時 v2・実行時 v1 の組合せ（消えた label の valueOf は実行時 IAE になる）
    classpath = runtimeWithoutProducer() + files(v1Jar)
    mainClass.set("io.github.projectmapk.swapuse2.MainKt")
}

tasks.register<JavaExec>("runV2") {
    classpath = runtimeWithoutProducer() + files(v2Jar)
    mainClass.set("io.github.projectmapk.swapuse2.MainKt")
}
