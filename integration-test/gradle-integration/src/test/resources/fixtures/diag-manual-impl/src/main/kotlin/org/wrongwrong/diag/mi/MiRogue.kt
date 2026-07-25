package org.wrongwrong.diag.mi

import kotlin.reflect.KClass

// TC-DIAG-068: 階層外クラスによる生成 Enumish の直接実装 → ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY
// （報告位置 = 階層外の手動実装の宣言。docs/概要.md §8・docs/コンパイラプラグイン設計01.md §7.2）
object MiRogue : MiSi.Enumish {
    override val label: String get() = "rogue"

    override val enumizedClass: KClass<out MiSi> get() = MiSi::class
}
