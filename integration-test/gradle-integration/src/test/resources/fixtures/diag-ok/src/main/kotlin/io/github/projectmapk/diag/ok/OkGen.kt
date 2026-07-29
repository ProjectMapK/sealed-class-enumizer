package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-05: 型パラメータ付き基底 / 末端（非発火・生成成立）
@Enumize
sealed class OkGen<T> {
    class BoxG<T>(val t: T) : OkGen<T>()

    class FixedG : OkGen<Unit>()
}
