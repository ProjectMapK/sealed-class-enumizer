package org.wrongwrong.mpp.fixtures

import kotlin.jvm.JvmInline
import org.wrongwrong.sealedClassEnumizer.Enumize

// value class 末端の boxing 挙動フィクスチャの MPP 版（docs/test/ケース05-境界横断.md XMP-34）。
// 基底 Valued / 生成 Enumish 型として扱う boxing がターゲット間で割れないことを観測する
@Enumize
sealed interface Valued {
    @JvmInline value class Vc(val s: String) : Valued

    data object None : Valued
}
