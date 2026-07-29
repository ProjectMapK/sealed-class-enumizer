// 基底不在ラウンド・多段中間チェーン・空⇔非空遷移のフィクスチャ本体
// （docs/test/ケース06-ビルド動態.md BLD-21/28/29/30）
plugins {
    kotlin("jvm")
    id("io.github.projectmapk.sealed-class-enumizer")
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("io.github.projectmapk.icrounds.MainKt")
}
