// 非 final 末端（V10）の下流サブタイプ定義側（docs/test/テスト戦略.md §4）。
// サブクラス化に @Enumize は要らないためプラグインは未適用
plugins { kotlin("jvm") }

group = "io.github.projectmapk"

version = "1.0-SNAPSHOT"

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":producer-jvm"))
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }
