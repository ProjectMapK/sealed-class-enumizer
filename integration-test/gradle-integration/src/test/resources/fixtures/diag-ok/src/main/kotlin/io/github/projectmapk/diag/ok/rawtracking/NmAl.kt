package io.github.projectmapk.diag.ok.rawtracking

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-31/32: raw 追跡表記の各形で候補判定が成立する（= kind が作られる）。
// 表記が file 単位の import に依存する形は別ファイルへ分ける:
// import 別名 = NmAlImported.kt・別 pkg typealias 明示 import = NmFarNoc.kt・star import = NmStarNoc.kt。
// 同一 pkg typealias の宣言は NmAlAlias.kt（別名の解決順を基底と分ける配置）

@Enumize
sealed interface NmAl

// 同一 pkg 直接名の末端 class（companion 無し）→ 自動生成成立
class NmDirNoc(val v: Int) : NmAl

// enum 末端も候補判定の対象（companion 自動生成で成立）
enum class NmEnumLeaf : NmAl {
    E1,
}

// FQN 表記の supertype でも候補判定が働き自動生成される
class NmFqFoo(val v: Int) : io.github.projectmapk.diag.ok.rawtracking.NmAl

// typealias 経由 supertype の末端 class（companion 無し）→ 成立
class NmAlNoc(val v: Int) : NmAlT

// typealias 経由 + 明示 companion の併用形 → 成立
class NmAlFoo(val v: Int) : NmAlT {
    companion object
}

// 外側スコープのネスト解決（supertype 名が外側宣言のスコープで解決される）
object NmOutHost {
    @Enumize
    sealed interface NBase

    class NLeaf(val v: Int) : NBase
}
