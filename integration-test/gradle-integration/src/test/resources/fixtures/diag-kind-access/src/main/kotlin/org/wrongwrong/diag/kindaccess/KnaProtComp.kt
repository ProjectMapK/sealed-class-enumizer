package org.wrongwrong.diag.kindaccess

// TC-DIAG-028: protected な既存 companion（open/abstract 末端） → ENUMIZE_KIND_NOT_ACCESSIBLE
abstract class KnaProtComp : KnaSi {
    protected companion object
}
