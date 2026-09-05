package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-23 用の基底
@Enumize
sealed interface InnerSi

// DIA-23: inner class 末端 → ENUMIZE_INNER_LEAF 単独
// （手動 asEnumish を併置しても以降のメンバー衝突検査はスキップ = MC / KTD 不在の固定）
class InnerHost {
    inner class BadLeaf : InnerSi {
        fun asEnumish(): Int = 0
    }
}
