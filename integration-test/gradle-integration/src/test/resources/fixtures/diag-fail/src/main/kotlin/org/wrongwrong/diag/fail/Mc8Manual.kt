package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-43 用: asEnumish の default 実装を持つ階層外 interface
interface Mc8Manual : Enumized<Mc8Si.Enumish> {
    override fun asEnumish(): Mc8Si.Enumish = Mc8Si.Real
}
