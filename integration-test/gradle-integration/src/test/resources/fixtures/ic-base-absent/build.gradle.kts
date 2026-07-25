// 基底不在の IC ラウンド用フィクスチャ（多ファイル sealed 階層 + プラグイン生成コード）
plugins {
    kotlin("jvm")
    id("org.wrongwrong.sealed-class-enumizer")
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("org.wrongwrong.baseabsent.MainKt")
}
