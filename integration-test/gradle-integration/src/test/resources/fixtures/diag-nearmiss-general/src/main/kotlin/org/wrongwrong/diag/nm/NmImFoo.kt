package org.wrongwrong.diag.nm

import org.wrongwrong.diag.nm.NmAl as NmAlI

// TC-DIAG-109: import エイリアス経由でも明示 companion があれば非発火（明示は常に完全なワークアラウンド）
class NmImFoo(val v: Int) : NmAlI {
    companion object
}
