package org.wrongwrong.diag.ok

// docs/test/ケース04-診断.md DIA-30: 階層外クラスの companion 単独末端 → COMPANION_LEAF_CONFLICT 非発火
class NmHost {
    companion object : NmHostBase
}
