package org.wrongwrong.mpp.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize
import kotlin.jvm.JvmInline

// value class 末端の boxing 挙動フィクスチャの MPP 版（docs/テストケース管理.md TC-MPP-012）。
// SI / 生成 Enumish 型として扱う boxing がターゲット間で割れないことを観測する
@Enumize
sealed interface Valued {
    @JvmInline
    value class Vc(val s: String) : Valued

    data object None : Valued
}
