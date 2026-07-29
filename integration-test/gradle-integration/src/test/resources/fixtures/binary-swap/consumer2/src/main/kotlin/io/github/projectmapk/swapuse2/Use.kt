package io.github.projectmapk.swapuse2

import io.github.projectmapk.swaplib.SI

// v2（3 末端）に対して網羅の else 無し kind-when。実行時 v1 では存在する kind だけが通り
// 全枝が解決される（docs/test/ケース06-ビルド動態.md BLD-40 の削除方向）
fun describeKind(kind: SI.Enumish): String = when (kind) {
    SI.Foo.Companion -> "foo"
    SI.Bar -> "bar"
    SI.Baz -> "baz"
}
