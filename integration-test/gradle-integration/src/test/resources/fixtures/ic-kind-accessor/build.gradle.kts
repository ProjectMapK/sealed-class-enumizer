// 参照不能 kind 用 IR-only アクセサの IC 決定性フィクスチャ（docs/概要.md §8・docs/コンパイラプラグイン設計02.md §4.3）
plugins {
    kotlin("jvm")
    id("io.github.projectmapk.sealed-class-enumizer")
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("io.github.projectmapk.ickacc.MainKt")
}
