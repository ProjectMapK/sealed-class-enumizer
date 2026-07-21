// 純消費側（プラグイン未適用）。producer の ABI 差分による再コンパイル誘発を観測する
plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":producer"))
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("org.wrongwrong.abiuse.MainKt")
}
