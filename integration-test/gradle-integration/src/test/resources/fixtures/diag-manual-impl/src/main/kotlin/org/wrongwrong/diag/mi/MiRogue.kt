package org.wrongwrong.diag.mi

import kotlin.reflect.KClass

// TC-DIAG-068: 階層外クラスによる生成 Enumish の直接実装 → ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY
// （docs/概要.md §8・設計01 §7.2 の現行仕様。テストケース管理.md MI-01 行の「非発火」は V1-(e) 反映前の記述）
object MiRogue : MiSi.Enumish {
    override val label: String get() = "rogue"

    override val enumizedClass: KClass<out MiSi> get() = MiSi::class
}
