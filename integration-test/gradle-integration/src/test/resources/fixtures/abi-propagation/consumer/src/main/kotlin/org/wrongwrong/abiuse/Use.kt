package org.wrongwrong.abiuse

import org.wrongwrong.abifix.Bar
import org.wrongwrong.abifix.Foo
import org.wrongwrong.abifix.SI

// 跨モジュール kind-when（else 無し）。コンパイル成立が V1-a、末端追加後の網羅性エラーが V1-b
// （docs/test/ケース06-ビルド動態.md BLD-34・docs/コンパイラプラグイン設計00.md §5.2）
fun describeKind(kind: SI.Enumish): String = when (kind) {
    Foo.Companion -> "foo"
    Bar -> "bar"
}
