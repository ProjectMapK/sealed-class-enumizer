package org.wrongwrong.fixtures.scope.target

import org.wrongwrong.sealedClassEnumizer.Enumize

// raw 追跡スコープ順の真基底（docs/test/ケース01-生成と実行時API.md API-51）。
// object Holder 内ネストの @Enumize 基底であり、単純名 "Base" が同一 pkg のトップレベル囮（Base.kt）と競合する
object Holder {
    @Enumize sealed interface Base
}
