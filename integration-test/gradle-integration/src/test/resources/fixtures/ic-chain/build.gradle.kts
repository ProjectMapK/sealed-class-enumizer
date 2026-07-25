// 多段中間 sealed チェーンのフィクスチャ本体（docs/コンパイラプラグイン設計00.md §6.2 の再帰展開・チェーン共連れ）
plugins {
    kotlin("jvm")
    id("org.wrongwrong.sealed-class-enumizer")
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("org.wrongwrong.chain.MainKt")
}
