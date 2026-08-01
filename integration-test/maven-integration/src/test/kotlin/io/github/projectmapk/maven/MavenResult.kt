package io.github.projectmapk.maven

// Maven ビルド 1 本の結果。出力は標準出力・標準エラーを合流させたもの
data class MavenResult(val exitCode: Int, val output: String) {
    val succeeded: Boolean
        get() = exitCode == 0

    // フィクスチャの JUnit が出す観測行（surefire がテストの標準出力をそのまま流す）
    fun observations(): List<String> =
        output.lines().map(String::trim).filter { it.startsWith(OBSERVATION_PREFIX) }

    // コンパイレーション毎に 1 回出る mojo のログ。適用範囲（production / test）の観測に使う
    fun appliedPluginCount(pluginName: String): Int =
        output.lines().count { it.contains("Applied plugin: '$pluginName'") }

    private companion object {
        const val OBSERVATION_PREFIX: String = "OUT: "
    }
}
