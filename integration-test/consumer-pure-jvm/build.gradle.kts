// プラグイン未適用の純消費側（docs/test/テスト戦略.md §4）。
// 生成 API がメタデータ経由で普通に参照できること（docs/概要.md §7）を検証する
plugins { kotlin("jvm") }

group = "io.github.projectmapk"

version = "1.0-SNAPSHOT"

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":producer-jvm"))
    testImplementation(kotlin("test"))
    // sealedSubclasses と entries の不一致観測（docs/test/ケース05-境界横断.md XMP-07）に使用。
    // ランタイム reflection 非依存の原則はプラグイン生成物側の話であり、テストからの観測には利用してよい
    testImplementation(kotlin("reflect"))
}

tasks.test { useJUnitPlatform() }
