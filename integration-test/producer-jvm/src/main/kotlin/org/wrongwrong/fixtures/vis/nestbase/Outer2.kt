package org.wrongwrong.fixtures.vis.nestbase

import org.wrongwrong.sealedClassEnumizer.Enumize

// private ネスト基底を外側クラス本体（可視範囲の内側）で利用する（docs/test/ケース02-可視性.md VIS-05。
// Outer2 本体内からは全 API 利用可・kind 単位 when は else 不要）
class Outer2 {
    @Enumize
    private sealed interface P {
        data object X : P

        data object Y : P
    }

    fun labels(): List<String> = P.Enumish.entries.map { it.label }

    fun resolve(label: String): String? = P.Enumish.valueOfOrNull(label)?.label

    fun pick(flag: Boolean): String {
        val value: P = if (flag) P.X else P.Y
        return when (value.asEnumish()) {
            P.X -> "x"
            P.Y -> "y"
        }
    }
}
