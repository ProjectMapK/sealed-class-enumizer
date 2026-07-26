package org.wrongwrong.fixtures.order.mid

// class 末端 Probe（自動生成 companion が kind）と、その内側ネストの data object 末端 Bb
// （docs/test/ケース03-順序.md §2.2・ORD-06 の inheritors 分岐プローブ）。
// kind ClassId 整列（inheritors 順）では Probe.Bb('B'=66) が Probe.Companion('C'=67) に先行する一方、
// entries（末端 ClassId 由来）では Probe が Bb に先行し、相対順が分岐する
class Probe : MidRoot {
    data object Bb : MidRoot
}
