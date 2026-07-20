package org.wrongwrong.mpp.fixtures.web

import org.wrongwrong.sealedClassEnumizer.Enumize

// 中間ソースセット（webMain = js / wasmJs 共有）に置く @Enumize 階層
// （docs/テストケース管理.md TC-MPP-046 の web 共有代替形）。中間ソースセットの
// metadata コンパイルでも生成が成立し、共有先の各 platform で box が動作することを観測する
@Enumize
sealed interface WebShared {
    data object W1 : WebShared

    data class W2(val v: Int) : WebShared
}
