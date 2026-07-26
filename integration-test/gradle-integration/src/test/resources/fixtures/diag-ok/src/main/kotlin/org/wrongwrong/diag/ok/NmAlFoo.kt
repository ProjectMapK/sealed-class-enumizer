package org.wrongwrong.diag.ok

// docs/test/ケース04-診断.md DIA-32: typealias 経由 + 明示 companion の併用形 → 成立
class NmAlFoo(val v: Int) : NmAlT {
    companion object
}
