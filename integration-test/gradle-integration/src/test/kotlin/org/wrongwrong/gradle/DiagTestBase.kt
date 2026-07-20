package org.wrongwrong.gradle

import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories

// 診断カタログ系テストの共通基盤（docs/テストケース管理.md G 軸・「Gradle TestKit 方針」）。
// 同一フィクスチャを参照する複数のテストメソッドが 1 回のビルド結果を共有して、TestKit の
// 起動回数を抑える（1 フィクスチャ = 1 ビルドで複数診断をまとめて検証する方針）。
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class DiagTestBase {
    @TempDir
    protected lateinit var workRoot: Path

    private val cache = mutableMapOf<String, BuildResult>()

    // フィクスチャを展開して buildAndFail し、その出力を返す（クラス内でフィクスチャ毎に 1 回だけ実行）
    protected fun failOutput(fixture: String, vararg tasks: String): String =
        cache.getOrPut(fixture) { TestKitHarness.buildAndFail(prepare(fixture), *tasks) }.output

    // フィクスチャを展開して build（成功）し、その出力を返す（非発火 near-miss 用）
    protected fun successOutput(fixture: String, vararg tasks: String): String =
        cache.getOrPut(fixture) { TestKitHarness.build(prepare(fixture), *tasks) }.output

    // IC 系（ビルド → 編集 → 再ビルド）のようにキャッシュを介さない場合の展開だけを行う
    protected fun prepare(fixture: String): Path {
        val dir = workRoot.resolve(fixture)
        dir.createDirectories()
        TestKitHarness.prepareFixture(fixture, dir)
        return dir
    }
}
