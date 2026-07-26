package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-23: inner class 末端 → ENUMIZE_INNER_LEAF 単独
// （手動 asEnumish を併置しても以降のメンバー衝突検査はスキップ = MMC / KTD 不在の固定）
class InnerHost {
    inner class BadLeaf : InnerSi {
        fun asEnumish(): Int = 0
    }
}
