package io.github.projectmapk.diag.ok

import io.github.projectmapk.diag.ok.NmAl as NmAlI

// docs/test/ケース04-診断.md DIA-32: import 別名経由 + 明示 companion の併用形 → 成立
class NmImFoo(val v: Int) : NmAlI {
    companion object
}
