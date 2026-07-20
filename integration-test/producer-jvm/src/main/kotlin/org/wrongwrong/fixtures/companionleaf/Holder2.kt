package org.wrongwrong.fixtures.companionleaf

// 名前つき companion（Zzz）が末端（TC-ORD-058）。companion 自身が末端の場合は宣言名が
// label と末端 ClassId（= 順序キー）の両方に効く
class Holder2 {
    companion object Zzz : Badge
}
