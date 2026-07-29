package io.github.projectmapk.probe.cclash

// docs/test/ケース04-診断.md DIA-66: companion 無し末端 class が非 companion のネスト宣言
// object Companion を持つ形。自動生成 companion の ClassId と衝突する帰結
// （REDECLARATION / ICE / シャドーイング）を固定する
class CcLeaf(val v: Int) : CcSi {
    object Companion
}
