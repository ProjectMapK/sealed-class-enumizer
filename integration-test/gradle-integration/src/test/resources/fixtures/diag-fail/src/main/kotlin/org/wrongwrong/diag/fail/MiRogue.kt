package org.wrongwrong.diag.fail

import kotlin.reflect.KClass

// docs/test/ケース04-診断.md DIA-56: 階層外 object の生成 Enumish 直接実装
// → ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY + 基底 FQN（報告位置 = supertype ref）
object MiRogue : MiSi.Enumish {
    override val label: String get() = "rogue"

    override val enumizedClass: KClass<out MiSi> get() = MiSi::class
}
