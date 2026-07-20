package org.wrongwrong.fixtures

import org.wrongwrong.sealedClassEnumizer.Enumize

// private 基底（エッジ §1「基底の可視性は制限ではない」）。生成 API は可視範囲（このファイル内）で
// すべて通常どおり使えることを、同一ファイルの internal 関数経由で観測する
@Enumize
private sealed interface PrivateBase {
    data object Hidden : PrivateBase

    data class Datum(val v: Int) : PrivateBase
}

internal fun observePrivateBaseLabels(): List<String> = PrivateBase.Enumish.entries.map { it.label }

internal fun observePrivateValueOf(label: String): String? =
    PrivateBase.Enumish.valueOfOrNull(label)?.label

internal fun pickPrivate(value: Int): String {
    val instance: PrivateBase = if (value == 0) PrivateBase.Hidden else PrivateBase.Datum(value)
    return when (instance.asEnumish()) {
        PrivateBase.Hidden -> "hidden"
        PrivateBase.Datum.Companion -> "datum"
    }
}
