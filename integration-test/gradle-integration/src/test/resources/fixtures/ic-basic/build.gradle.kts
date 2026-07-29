// IC 回帰フィクスチャ本体（docs/コンパイラプラグイン設計00.md §5.3・docs/test/ケース06-ビルド動態.md §2）
plugins {
    kotlin("jvm")
    id("io.github.projectmapk.sealed-class-enumizer")
}

// 実行時挙動（entries / valueOf / kind-when）を標準出力の OUT: 行で観測するエントリポイント
tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("io.github.projectmapk.icfix.MainKt")
}
