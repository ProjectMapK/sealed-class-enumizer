package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-25: 基底より広い末端 + internal companion → ENUMIZE_KIND_TYPE_NOT_DENOTABLE
class KtdLeaf : KtdSi {
    internal companion object
}
