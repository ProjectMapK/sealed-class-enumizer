// 決定性フィクスチャ本体（順序境界 + toString 2 原則 + valueOf 失敗メッセージを 1 階層に集約）
plugins {
    kotlin("jvm")
    id("io.github.projectmapk.sealed-class-enumizer")
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("io.github.projectmapk.det.MainKt")
}
