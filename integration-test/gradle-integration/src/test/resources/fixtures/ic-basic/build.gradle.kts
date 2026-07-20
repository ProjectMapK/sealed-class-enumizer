// IC 回帰フィクスチャ本体（docs/コンパイラプラグイン設計00.md §5.3・docs/テストケース管理.md C 軸）
plugins {
    kotlin("jvm")
    id("org.wrongwrong.sealed-class-enumizer")
}

// 実行時挙動（entries / valueOf / kind-when）を標準出力の OUT: 行で観測するエントリポイント
tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("org.wrongwrong.icfix.MainKt")
}
