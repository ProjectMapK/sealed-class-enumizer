// 参照不能 kind 用 IR-only アクセサの IC 決定性フィクスチャ（概要 §8・設計02 §4.3）
plugins {
    kotlin("jvm")
    id("org.wrongwrong.sealed-class-enumizer")
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("org.wrongwrong.ickacc.MainKt")
}
