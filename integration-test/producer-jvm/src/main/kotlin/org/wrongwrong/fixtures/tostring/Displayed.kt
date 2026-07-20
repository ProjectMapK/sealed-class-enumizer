package org.wrongwrong.fixtures.tostring

import org.wrongwrong.sealedClassEnumizer.Enumize

// toString の 2 原則のうち「継承経路上の具象 toString」の枝（TC-LEAF-060 / TC-BOX-045/046/047）。
// 原則 1: 継承した具象実装（Any 以外）を持つ kind には生成しない
@Enumize
sealed interface Displayed {
    // companion が具象 toString を継承 → 生成しない（"base-display" が残る）
    class ViaBase(val v: Int) : Displayed {
        companion object : BaseDisplay()
    }

    // 末端 object 自身が具象 toString を継承 → 生成しない
    object ObjViaBase : BaseDisplay(), Displayed

    // final 継承 toString でも生成スキップにより override 衝突は起きない
    class ViaFixed(val v: Int) : Displayed {
        companion object : FixedDisplay()
    }

    // 抽象再宣言 + kind 側の手動 toString で成立（手動が無ければコンパイラ本体の抽象未実装エラー）
    class ViaDemand(val v: Int) : Displayed {
        companion object : Demanding() {
            override fun toString(): String = "answered"
        }
    }
}
