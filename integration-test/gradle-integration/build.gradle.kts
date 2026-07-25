// Gradle TestKit ホスト（docs/テストケース管理.md モジュール一覧）。
// resources/fixtures 配下の合成ビルドを GradleRunner で駆動し、IC 回帰・決定性・
// 跨モジュール負値診断・ABI 伝播・旧バイナリ差し替え・基底不在ラウンドを検証する
plugins {
    kotlin("jvm")
}

group = "org.wrongwrong"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
}

// フィクスチャの展開先（IcTestSupport が使う）。テスト毎に一意なディレクトリを掘るため、
// 実行を重ねると際限なく溜まり、テスト時間が実行毎に悪化する（実測: 蓄積なし 255 秒に対し
// 10 回分の蓄積で 763 秒）。失敗解析のため実行後は残し、次回の実行開始時に作り直す
// （docs/テストケース管理.md フィクスチャ展開先の回収）
val fixtureWorkRoot = layout.buildDirectory.dir("testkit-fixtures")

// TestKit の 1 テストは別プロセスの Gradle デーモンと Kotlin デーモンを 1 本ずつ占有する。
// 並行数の上限は CPU ではなくデーモンの常駐メモリで決まるため、コア数の 1/3 を採る
val testKitForks = (Runtime.getRuntime().availableProcessors() / 3).coerceIn(1, 8)

// 展開先の作り直しはテスト本体と分けて前段のタスクで行う（test の出力準備と混ざらないようにする）。
// 直前の実行が残した TestKit のデーモンが Windows でファイルを掴んだままのことがあるため、
// 削除は best-effort とする（掴まれた分は次回以降の実行で回収される）
val cleanFixtureWorkRoot = tasks.register("cleanFixtureWorkRoot") {
    val workRoot = fixtureWorkRoot
    doLast {
        workRoot.get().asFile.deleteRecursively()
    }
}

tasks.test {
    dependsOn(cleanFixtureWorkRoot)
    // フィクスチャはプラグイン一式をローカル Maven から解決するため、テスト前に公開しておく
    // （docs/テストケース管理.md Gradle TestKit 方針の local-repo 経路）
    dependsOn(gradle.includedBuild("sealed-class-enumizer").task(":publishAllToMavenLocal"))
    useJUnitPlatform()
    systemProperty("enumizer.fixtureWorkRoot", fixtureWorkRoot.get().asFile.absolutePath)
    // テストクラス単位の並行実行。クラス内は直列のままなので、DiagTestBase の
    // 「1 フィクスチャ = 1 ビルド」共有とフィクスチャ名で固定ディレクトリを掘る前提は保たれる
    maxParallelForks = testKitForks
    // 各フォークは駆動した全ビルドの出力（BuildResult.output）を保持する
    maxHeapSize = "2g"
    // TestKit ビルドは長時間になるためテスト毎の結果を逐次流す
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}
