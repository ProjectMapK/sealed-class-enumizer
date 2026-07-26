package org.wrongwrong.diag.fail

import kotlin.reflect.KClass

// docs/test/ケース04-診断.md DIA-43 用: enumizedClass の具象 default を持つ階層外 interface
interface Mc9Prov {
    val enumizedClass: KClass<out Mc9Si> get() = Mc9Si::class
}
