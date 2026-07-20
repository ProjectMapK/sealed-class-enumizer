// Java 消費側（docs/テストケース管理.md モジュール一覧・docs/概要.md §3「Java からの利用」）。
// Java 21 パターンマッチング switch（JEP 441）のためこのモジュールだけ toolchain 21 を使う
plugins {
    java
}

group = "org.wrongwrong"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    testImplementation(project(":producer-jvm"))
    // 暫定: gradle-plugin が runtime-api を implementation で自動追加するため利用側へ伝播しない
    // （docs/修正方針案.md #1。api 化の修正後はこの宣言は不要になる）
    testImplementation("org.wrongwrong:runtime-api:1.0-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
