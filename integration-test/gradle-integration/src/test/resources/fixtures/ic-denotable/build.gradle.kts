// 基底より広い末端の可視性編集フィクスチャ（docs/エッジケースへの対応方針.md §1.3・
// docs/コンパイラプラグイン設計01.md §5.4 規則 3・docs/test/ケース06-ビルド動態.md BLD-18）
plugins {
    kotlin("jvm")
    id("org.wrongwrong.sealed-class-enumizer")
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("org.wrongwrong.widerfix.MainKt")
}
