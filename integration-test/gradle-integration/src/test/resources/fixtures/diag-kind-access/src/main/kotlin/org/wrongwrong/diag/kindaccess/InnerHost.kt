package org.wrongwrong.diag.kindaccess

// TC-DIAG-024: 末端が inner class → ENUMIZE_INNER_LEAF（inner class は companion を持てない）
class InnerHost {
    inner class BadLeaf : InnerSi
}
