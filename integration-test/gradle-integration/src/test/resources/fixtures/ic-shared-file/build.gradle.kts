// 同一ファイル 2 階層フィクスチャ本体（P3 の file-granularity 境界）
plugins {
    kotlin("jvm")
    id("org.wrongwrong.sealed-class-enumizer")
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("org.wrongwrong.shared.MainKt")
}
