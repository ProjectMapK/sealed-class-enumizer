package io.github.projectmapk.swapuse2

import io.github.projectmapk.swaplib.SI

// 実行時観測: entries の件数・並び・valueOf・kind-when を実行時クラスパスの版で解決する
fun main() {
    val entries = SI.Enumish.entries
    println("OUT:COUNT=" + entries.size)
    println("OUT:ENTRIES=" + entries.joinToString(",") { it.label })
    println("OUT:PROBE_BAZ=" + probeBaz())
    println("OUT:WHEN=" + entries.joinToString(",") { kind ->
        kind.label + "->" + runCatching { describeKind(kind) }.getOrElse { it::class.simpleName ?: "?" }
    })
}

private fun probeBaz(): String = try {
    SI.Enumish.valueOf("Baz").label
} catch (e: IllegalArgumentException) {
    "IAE"
}

// v2（3 末端）に対して網羅の else 無し kind-when。実行時 v1 では存在する kind だけが通り
// 全枝が解決される（docs/test/ケース06-ビルド動態.md BLD-40 の削除方向）
fun describeKind(kind: SI.Enumish): String = when (kind) {
    SI.Foo.Companion -> "foo"
    SI.Bar -> "bar"
    SI.Baz -> "baz"
}
