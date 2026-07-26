package org.wrongwrong.diag.fail

import kotlin.reflect.KClass

// docs/test/ケース04-診断.md DIA-43 用: enumizedClass の具象 default を持つ階層外 interface
interface EcProv {
    val enumizedClass: KClass<out EcSi> get() = EcSi::class
}
