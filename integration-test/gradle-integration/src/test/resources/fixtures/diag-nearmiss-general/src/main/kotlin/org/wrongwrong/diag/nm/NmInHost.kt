package org.wrongwrong.diag.nm

// TC-DIAG-097: inner class が非 final 末端のサブタイプ（階層外・末端ではない） → INNER_LEAF 非発火（吸収）
class NmInHost {
    inner class Tri : NmIn.Poly()
}
