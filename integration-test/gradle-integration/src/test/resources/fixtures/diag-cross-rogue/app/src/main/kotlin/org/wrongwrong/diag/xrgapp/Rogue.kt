package org.wrongwrong.diag.xrgapp

import org.wrongwrong.diag.xrg.XrgSi
import kotlin.reflect.KClass

// TC-DIAG-071: 別モジュールからの生成 Enumish 手動実装 → 生成 Enumish は sealed（V1）のため言語側で不可
object Rogue : XrgSi.Enumish {
    override val label: String get() = "rogue"

    override val enumizedClass: KClass<out XrgSi> get() = XrgSi::class
}
