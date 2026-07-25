// 非 final 末端（V10）の下流サブタイプ定義側（docs/テストケース管理.md モジュール一覧）。
// サブクラス化に @Enumize は要らないためプラグインは未適用
plugins { kotlin("jvm") }

group = "org.wrongwrong"

version = "1.0-SNAPSHOT"

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":producer-jvm"))
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }
