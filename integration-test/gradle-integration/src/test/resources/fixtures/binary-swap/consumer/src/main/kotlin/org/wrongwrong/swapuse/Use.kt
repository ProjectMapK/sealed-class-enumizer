package org.wrongwrong.swapuse

import org.wrongwrong.swaplib.SI

// v1（2 末端）に対して網羅の else 無し kind-when。実行時 v2 で未知 kind を通すと
// NoWhenBranchMatchedException になる（docs/テストケース管理.md TC-XM-053）
fun describeKind(kind: SI.Enumish): String = when (kind) {
    SI.Foo.Companion -> "foo"
    SI.Bar -> "bar"
}
