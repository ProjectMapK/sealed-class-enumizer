package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-05: @Enumize 付き local class は全入口で一貫除外
// → 非 sealed でも NOT_SEALED を出さず無診断で素通りする
fun okLocalHost(): Int {
    @Enumize
    class LocalPlain

    return LocalPlain().hashCode()
}
