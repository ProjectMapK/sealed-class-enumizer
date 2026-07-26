package org.wrongwrong.diag.fail

import kotlin.reflect.KClass

// docs/test/ケース04-診断.md DIA-56: 中間 sealed の companion による実装
// → kind companion 免除の不成立側（MIOH 発火）
sealed interface MiMid : MiSi {
    companion object : MiSi.Enumish {
        override val label: String get() = "mid"

        override val enumizedClass: KClass<out MiSi> get() = MiMid::class
    }
}
