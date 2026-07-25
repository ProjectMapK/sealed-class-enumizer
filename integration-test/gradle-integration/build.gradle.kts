// Gradle TestKit ホスト（docs/テストケース管理.md モジュール一覧）。
// resources/fixtures 配下の合成ビルドを GradleRunner で駆動し、IC 回帰・決定性・
// 跨モジュール負値診断・ABI 伝播・旧バイナリ差し替え・KT-86121 を検証する
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

// 展開先の作り直しはテスト本体と分けて前段のタスクで行う（test の出力準備と混ざらないようにする）。
// 直前の実行が残した TestKit のデーモンが Windows でファイルを掴んだままのことがあるため、
// 削除は best-effort とする（掴まれた分は次回以降の実行で回収される）
val cleanFixtureWorkRoot by tasks.registering {
    val workRoot = fixtureWorkRoot
    doLast {
        workRoot.get().asFile.deleteRecursively()
    }
}

tasks.test {
    dependsOn(cleanFixtureWorkRoot)
    useJUnitPlatform()
    // フィクスチャの settings.gradle.kts が pluginManagement { includeBuild(<親ビルド>) } で
    // 本物のプラグインを解決するための絶対パス（docs/テストケース管理.md Gradle TestKit 方針）
    systemProperty("enumizer.parentBuild", rootDir.parentFile.absolutePath)
    systemProperty("enumizer.fixtureWorkRoot", fixtureWorkRoot.get().asFile.absolutePath)
    // TestKit ビルドは長時間になるためテスト毎の出力を逐次流す
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}
