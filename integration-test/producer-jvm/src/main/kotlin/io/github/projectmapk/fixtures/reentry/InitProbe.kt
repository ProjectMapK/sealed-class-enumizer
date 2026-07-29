package io.github.projectmapk.fixtures.reentry

// 遅延初期化の順序センサ（docs/test/ケース01-生成と実行時API.md API-44）。
// kind の初期化が走ったことを記録する
object InitProbe {
    val events: MutableList<String> = mutableListOf()
}
