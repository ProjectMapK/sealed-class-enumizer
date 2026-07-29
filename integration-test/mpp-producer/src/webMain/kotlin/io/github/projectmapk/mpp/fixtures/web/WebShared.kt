package io.github.projectmapk.mpp.fixtures.web

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 中間ソースセット（webMain = js / wasmJs 共有）に置く @Enumize 階層
// （docs/test/ケース05-境界横断.md XMP-40 の web 共有代替形）。中間ソースセットの
// metadata コンパイルでも生成が成立し、共有先の各 platform で box が動作することを観測する
@Enumize
sealed interface WebShared {
    data object W1 : WebShared

    data class W2(val v: Int) : WebShared
}
