package io.github.projectmapk.fixtures.vis.priv

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.label

// private 基底の同一ファイル完結フィクスチャ（docs/test/ケース02-可視性.md VIS-01/VIS-10/VIS-20/VIS-22）。
// private トップレベル基底はファイル内でのみ可視のため、末端・観測関数とも同一ファイル配置が言語上の要請となる。
@Enumize
private sealed interface PrivBase {
    data object Hidden : PrivBase

    data class Datum(val v: Int) : PrivBase

    // private 基底内の internal companion（VIS-10: eff(C) = eff(L) = private の実効同値で規則 1）
    class Kept(val v: Int) : PrivBase {
        internal companion object
    }
}

// 基底（private）より広い public 末端（VIS-20 = E-2 の private 基底版。
// 露出検査は interface 実装に働かないため成立し、値/kind API は別ファイルから規則 1 で利用できる）
class Wide(val v: Int) : PrivBase {
    companion object
}

// --- 同一ファイルの internal 観測関数（VIS-01: private 基底の全 API はファイル内で完全動作） ---

internal fun observePrivBaseLabels(): List<String> = PrivBase.Enumish.entries.map { it.label }

internal fun observePrivBaseValueOf(label: String): String? =
    PrivBase.Enumish.valueOfOrNull(label)?.label

internal fun observePrivBaseValueLabels(): List<String> =
    listOf(PrivBase.Hidden.label, PrivBase.Datum(1).label, PrivBase.Kept(2).label, Wide(3).label)

// VIS-10: 規則 1 の検査は「具体型（PrivBase.Kept.Companion）で受けられること」自体が担う
internal fun keptStaysOnRule1(): Boolean {
    val kind: PrivBase.Kept.Companion = PrivBase.Kept(1).asEnumish()
    return kind === PrivBase.Kept.Companion
}

// VIS-22: 全 kind 可視の位置（同一ファイル内）では kind-when の else を省略できる
internal fun pickPrivBase(ordinal: Int): String {
    val value: PrivBase =
        when (ordinal) {
            0 -> PrivBase.Hidden
            1 -> PrivBase.Datum(1)
            2 -> PrivBase.Kept(2)
            else -> Wide(3)
        }
    return when (value.asEnumish()) {
        PrivBase.Hidden -> "hidden"
        PrivBase.Datum.Companion -> "datum"
        PrivBase.Kept.Companion -> "kept"
        Wide.Companion -> "wide"
    }
}
