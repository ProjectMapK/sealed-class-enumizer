package org.wrongwrong.fixtures.placement

// 階層と無関係なクラスの内側に末端をネストする（sealed の末端は同一パッケージ内の任意クラスにネスト可能）。
// 共通接頭辞 p.Crate. のため相対順は単純名昇順（TC-ORD-003）
class Crate {
    data object CrBbb : Cage

    data object CrAaa : Cage
}
