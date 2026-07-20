// KT-86121 再現形フィクスチャ（多ファイル sealed 階層 + プラグイン生成コード）
plugins {
    kotlin("jvm")
    id("org.wrongwrong.sealed-class-enumizer")
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("org.wrongwrong.kt86121.MainKt")
}
