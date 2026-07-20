package org.wrongwrong.sweep.fail

// TC-MAN-065: 末端が階層外 interface から asEnumish の default 実装を継承
// → ENUMIZE_MANUAL_MEMBER_CONFLICT（label 版 = TC-DIAG-047 の asEnumish 対応物）
data class SwMhLeaf(val v: Int) : SwMhSi, SwMhManual {
    companion object
}
