// 非 final 末端（V10）の下流サブタイプ定義側（docs/テストケース管理.md モジュール一覧）。
// サブクラス化に @Enumize は要らないためプラグインは未適用
plugins {
    kotlin("jvm")
}

group = "org.wrongwrong"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":producer-jvm"))
    // 暫定: gradle-plugin が runtime-api を implementation で自動追加するため利用側へ伝播しない
    // （docs/修正方針案.md #1。api 化の修正後はこの宣言は不要になる）
    implementation("org.wrongwrong:runtime-api:1.0-SNAPSHOT")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
