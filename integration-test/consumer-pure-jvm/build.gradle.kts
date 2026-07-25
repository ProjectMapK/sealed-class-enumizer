// プラグイン未適用の純消費側（docs/テストケース管理.md モジュール一覧）。
// 生成 API がメタデータ経由で普通に参照できること（docs/概要.md §7）を検証する
plugins { kotlin("jvm") }

group = "org.wrongwrong"

version = "1.0-SNAPSHOT"

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":producer-jvm"))
    testImplementation(kotlin("test"))
    // sealedSubclasses と entries の不一致観測（TC-XM-047）に使用。
    // ランタイム reflection 非依存の原則はプラグイン生成物側の話であり、テストからの観測には利用してよい
    testImplementation(kotlin("reflect"))
}

tasks.test { useJUnitPlatform() }
