package org.wrongwrong.diag.kindaccess

// TC-DIAG-027: private な既存 companion → ENUMIZE_KIND_NOT_ACCESSIBLE（基底本体スコープから参照不能）
class KnaPrivComp(val v: Int) : KnaSi {
    private companion object
}
