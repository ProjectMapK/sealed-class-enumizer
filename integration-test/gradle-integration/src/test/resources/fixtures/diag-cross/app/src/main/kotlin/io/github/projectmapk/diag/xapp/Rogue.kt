package io.github.projectmapk.diag.xapp

import io.github.projectmapk.diag.xlib.XrgSi
import kotlin.reflect.KClass

// docs/test/ケース04-診断.md DIA-57: 別 module からの生成 Enumish 手動実装
// → 言語の sealed 別モジュール制約と ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY の併発（適用側）
object Rogue : XrgSi.Enumish {
    override val label: String get() = "rogue"

    override val enumizedClass: KClass<out XrgSi> get() = XrgSi::class
}
