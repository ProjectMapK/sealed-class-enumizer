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

tasks.test {
    useJUnitPlatform()
    // フィクスチャの settings.gradle.kts が pluginManagement { includeBuild(<親ビルド>) } で
    // 本物のプラグインを解決するための絶対パス（docs/テストケース管理.md Gradle TestKit 方針）
    systemProperty("enumizer.parentBuild", rootDir.parentFile.absolutePath)
    // TestKit ビルドは長時間になるためテスト毎の出力を逐次流す
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}
