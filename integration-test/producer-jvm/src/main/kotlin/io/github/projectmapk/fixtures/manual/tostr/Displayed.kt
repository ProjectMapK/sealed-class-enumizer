package io.github.projectmapk.fixtures.manual.tostr

import io.github.projectmapk.sealedClassEnumizer.Enumize

// toString 2 原則と label シャドーイングの統合フィクスチャ
// （docs/test/ケース01-生成と実行時API.md API-33〜API-35・API-37〜API-39）。
//
// 期待 entries（label 列。FQN UTF-16 序数順）:
// [Manual, ObjViaBase, Styled, Tagged, ViaBase, ViaDemand, ViaFixed]
@Enumize
sealed interface Displayed {
    // companion が具象 toString を継承 → 生成しない（"base-display" が残る。API-35）
    class ViaBase(val v: Int) : Displayed {
        companion object : BaseDisplay()
    }

    // 末端 object 自身が具象 toString を継承 → 生成しない（API-35）
    object ObjViaBase : BaseDisplay(), Displayed

    // final 継承 toString でも生成スキップにより override 衝突は起きない（API-35）
    class ViaFixed(val v: Int) : Displayed {
        companion object : FixedDisplay()
    }

    // 抽象再宣言 + kind 側の手動 toString で充足（API-37。手動が無ければ言語の抽象未実装エラー）
    class ViaDemand(val v: Int) : Displayed {
        companion object : Demanding() {
            override fun toString(): String = "answered"
        }
    }

    // kind（data object 末端）自身の手動 toString — 生成しない（API-34）
    data object Manual : Displayed {
        override fun toString(): String = "custom"
    }

    // kind（companion）の手動 toString — 生成しない（API-34）
    data class Styled(val v: Int) : Displayed {
        companion object {
            override fun toString(): String = "companion-custom"
        }
    }

    // メンバー label が Enumized<T>.label 拡張を呼び出し点でシャドーイングする（API-39。
    // ENUMIZE_EXTENSION_SHADOWED 警告の発火照合はケース04 が正典）
    data class Tagged(val label: String) : Displayed
}

// --- 末端 / kind が継承する toString 供給元 ---

// Any 以外の具象 toString を提供する親クラス（API-35 の継承元）
open class BaseDisplay {
    override fun toString(): String = "base-display"
}

// final override の toString を持つ親クラス（API-35。生成をスキップするため final 衝突は起きない）
open class FixedDisplay {
    final override fun toString(): String = "fixed-display"
}

// toString を抽象として再宣言する親クラス（API-37。生成は充足を肩代わりせず、kind 側の手動実装が必要）
abstract class Demanding {
    abstract override fun toString(): String
}
