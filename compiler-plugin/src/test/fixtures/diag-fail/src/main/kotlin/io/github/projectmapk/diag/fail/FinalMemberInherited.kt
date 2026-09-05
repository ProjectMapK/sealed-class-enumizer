package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-70: クラス supertype の final 具象継承で MEMBER_CONFLICT。
// 階層外の直接継承・中間クラス介在・引数なし関数 asEnumish・手動 kind companion（報告位置 = companion）・
// 階層内 sealed class 基底の ctor プロパティの各形

@Enumize
sealed interface FiSi

// final 具象 label を持つ階層外クラス（open 修飾なし = final メンバー）
open class FiOut {
    val label: String get() = "fixed"
}

// final label を FiOut から継承する中間の階層外クラス
open class FiMid : FiOut()

// 階層外クラスの final 具象 label を継承する末端 object
object FiLeaf : FiOut(), FiSi

// 中間クラス介在でも supertype 閉包の final label 継承で MC
object FiViaMid : FiMid(), FiSi

// 手動 kind companion による階層外 final label 継承 → MC（報告位置 = companion）
class FiCls(val v: Int) : FiSi {
    companion object : FiOut()
}

// final 具象 asEnumish（引数なし関数）を持つ階層外クラス
open class FiAsOut {
    fun asEnumish(): FiSi.Enumish = FiAsLeaf
}

object FiAsLeaf : FiAsOut(), FiSi

// final の ctor プロパティ label を自身が持つ階層内基底
// （label 宣言位置には ES 警告 = DIA-37 が併発する）
@Enumize
sealed class FiSc(val label: String)

// 階層内 sealed class 基底の final ctor プロパティ label 継承でも MC
object FiScLeaf : FiSc("ctor")
