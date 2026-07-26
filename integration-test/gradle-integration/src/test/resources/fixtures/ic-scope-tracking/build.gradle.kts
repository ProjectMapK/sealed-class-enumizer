// スコープ解決の跨ファイル編集フィクスチャ本体（docs/test/ケース06-ビルド動態.md BLD-45）
plugins {
    kotlin("jvm")
    id("org.wrongwrong.sealed-class-enumizer")
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("org.wrongwrong.icscope.MainKt")
}
