package org.wrongwrong.mpp.fixtures.tostring

// kind（companion）の継承経路に置く「Any 以外の具象 toString」（docs/概要.md §4 原則 1 の
// 「継承実装」ケース）。Show.Inherited の companion がこれを継承する
abstract class Loud {
    override fun toString(): String = "loud"
}
