package io.github.projectmapk.sealedClassEnumizer.compiler.testing

// フィクスチャを構成する 1 コンパイル単位（Gradle の 1 compilation に対応する）。
// 単位は宣言順に実行され、後段は前段までの出力をクラスパスに持つ
data class FixtureCompilation(
    // フィクスチャディレクトリからのソースルート相対パス
    val sourceRoot: String,
    // コンパイラプラグインを適用するか（未適用 = 純消費側のコンパイル）
    val pluginApplied: Boolean,
    // 直前の単位を friend として扱うか（同一 module の test コンパイレーション相当）
    val friendOfPrevious: Boolean,
) {
    companion object {
        // 単一モジュールのフィクスチャ（プラグイン適用）
        fun main(): FixtureCompilation =
            FixtureCompilation("src/main/kotlin", pluginApplied = true, friendOfPrevious = false)

        // main に続く test ソースセット（プラグイン適用・main と friend 関係）
        fun test(): FixtureCompilation =
            FixtureCompilation("src/test/kotlin", pluginApplied = true, friendOfPrevious = true)

        // 別モジュール（プラグイン適用の生成側）
        fun producer(module: String): FixtureCompilation =
            FixtureCompilation(
                "$module/src/main/kotlin",
                pluginApplied = true,
                friendOfPrevious = false,
            )

        // 別モジュール（プラグイン未適用の消費側）
        fun consumer(module: String): FixtureCompilation =
            FixtureCompilation(
                "$module/src/main/kotlin",
                pluginApplied = false,
                friendOfPrevious = false,
            )
    }
}
