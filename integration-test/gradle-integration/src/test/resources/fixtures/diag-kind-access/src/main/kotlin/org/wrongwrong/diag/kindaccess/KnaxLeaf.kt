package org.wrongwrong.diag.kindaccess

// TC-DIAG-088: 基底より広い末端 + private companion → doc 上は KIND_NOT_ACCESSIBLE のみ（DENOTABLE は前提未達）
class KnaxLeaf : KnaxSi {
    private companion object
}
