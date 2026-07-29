package io.github.projectmapk.icfix

// 実行時観測。OUT: 行を TestKit 側が抽出して clean ビルドの基準値と比較する
fun main() {
    println("OUT:ENTRIES=" + SI.Enumish.entries.joinToString(",") { it.label })
    println("OUT:TI_ENTRIES=" + TI.Enumish.entries.joinToString(",") { it.label })
    println("OUT:NB_ENTRIES=" + NbHost.NB.Enumish.entries.joinToString(",") { it.label })
    println("OUT:DESCRIBE=" + describe(Foo(1)) + "," + describe(Bar) + "," + describe(Outer.Leaf))
    println("OUT:PROBE_FOO=" + probe("Foo"))
    println("OUT:PROBE_LEAF=" + probe("Leaf"))
    println("OUT:TRY_BAZ=" + probe("Baz"))
    println("OUT:NOLABEL=" + probe("NoSuch"))
}

private fun probe(label: String): String = try {
    SI.Enumish.valueOf(label).label
} catch (e: IllegalArgumentException) {
    "IAE:" + e.message
}
