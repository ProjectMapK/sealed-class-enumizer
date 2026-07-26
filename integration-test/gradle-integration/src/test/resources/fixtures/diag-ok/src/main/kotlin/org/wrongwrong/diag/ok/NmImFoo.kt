package org.wrongwrong.diag.ok

import org.wrongwrong.diag.ok.NmAl as NmAlI

// docs/test/ケース04-診断.md DIA-32: import 別名経由 + 明示 companion の併用形 → 成立
class NmImFoo(val v: Int) : NmAlI {
    companion object
}
