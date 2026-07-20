package org.wrongwrong.diag.kindaccess

// TC-DIAG-032: 基底より広い末端 + internal companion → ENUMIZE_KIND_TYPE_NOT_DENOTABLE（§5.4 規則 3）
class KtdLeaf : KtdSi {
    internal companion object
}
