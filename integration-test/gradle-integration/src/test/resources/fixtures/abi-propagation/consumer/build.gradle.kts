// 純消費側（プラグイン未適用）。producer の ABI 差分による再コンパイル誘発を観測する
plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":producer"))
    // 暫定: gradle-plugin の runtime-api 自動追加が implementation のため、未適用消費側では
    // 明示宣言が必要（docs/修正方針案.md #1。api 公開へ修正後は不要になる）
    implementation("org.wrongwrong:runtime-api:1.0-SNAPSHOT")
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("org.wrongwrong.abiuse.MainKt")
}
