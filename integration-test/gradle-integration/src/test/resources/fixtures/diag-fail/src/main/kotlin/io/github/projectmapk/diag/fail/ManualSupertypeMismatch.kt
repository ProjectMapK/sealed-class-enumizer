package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumish
import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-46〜49: Enumized<T> の T が自階層の生成 Enumish と一致しない形
// → ENUMIZE_MANUAL_SUPERTYPE_MISMATCH（報告位置 = supertype ref）。
// 直接継承・自作 interface 経由の間接継承・末端側・E-1 適格/非適格 K の各亜種を同居させる

// DIA-46/47/48 用の「別の Enumish 型」
interface MsWrong : Enumish

// --- DIA-46: 基底の Enumized<別型> 直接継承 ---

@Enumize
sealed interface Ms1Si : Enumized<MsWrong> {
    data object L1 : Ms1Si
}

// --- DIA-47: 自作 interface 経由（supertypeClosure）の不一致 ---

interface Ms2MyBase : Enumized<MsWrong>

@Enumize
sealed interface Ms2Si : Ms2MyBase {
    data object L2 : Ms2Si
}

// --- DIA-48: 階層メンバー（末端）の Enumized<別型> ---

@Enumize
sealed interface MsLeafSi

data class MsLeaf(val v: Int) : MsLeafSi, Enumized<MsWrong> {
    companion object
}

// --- DIA-49: E-1 適格 K でも非適格 K 6 亜種でも v1 は一律 MSM ---

// 適格 K（非 sealed interface・基底 Enumish サブタイプ・具象なし）
interface Ms3Kind : Enumish

@Enumize
sealed interface Ms3Si : Enumized<Ms3Kind> {
    data object L3 : Ms3Si
}

// 非適格 K（class 亜種）
abstract class Ms4K : Enumish

@Enumize
sealed interface Ms4Si : Enumized<Ms4K> {
    data object L4 : Ms4Si
}

// 非適格 K（sealed 亜種）
sealed interface Ms5K : Enumish

@Enumize
sealed interface Ms5Si : Enumized<Ms5K> {
    data object L5 : Ms5Si
}

// 非適格 K（supertype 経路内に自階層型 LeafI を含む）。
// K は interface 末端経由で構成し MIOH 非併発 = 1 宣言 1 診断を維持する
interface Ms6K : Enumish, Ms6Si.LeafI

@Enumize
sealed interface Ms6Si : Enumized<Ms6K> {
    interface LeafI : Ms6Si
}

// 非適格 K（基底より狭い可視性。interface supertype は露出検査の対象外のため言語エラーには掛からない）
internal interface Ms7K : Enumish

@Enumize
sealed interface Ms7Si : Enumized<Ms7K> {
    data object L7 : Ms7Si
}

// 非適格 K（経路内具象実装 = 具象メンバーを持つ）
interface Ms8K : Enumish {
    override val label: String get() = "k8"
}

@Enumize
sealed interface Ms8Si : Enumized<Ms8K> {
    data object L8 : Ms8Si
}

// 非適格 K（非 Enumish サブタイプ。Enumized の上限境界にも反するため言語エラーが併発する形。
// 発火の事実のみ固定する）
interface Ms9K

@Enumize
sealed interface Ms9Si : Enumized<Ms9K> {
    data object L9 : Ms9Si
}
