package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-19/DIA-24: 単一末端サブタイプ（inner 含む）は吸収され
// AMBIGUOUS_KIND / INNER_LEAF 非発火。非 inner のネスト末端も非発火

// 非 final 末端 + その単一サブタイプ
@Enumize
sealed interface NmAbs {
    abstract class Poly2 : NmAbs {
        companion object
    }
}

class NmAbsSub : NmAbs.Poly2()

// 非 final 末端 + inner class のサブタイプ
@Enumize
sealed interface NmIn {
    abstract class Poly : NmIn {
        companion object
    }
}

class NmInHost {
    inner class Tri : NmIn.Poly()
}

// 非 inner のネスト末端（DIA-24）
@Enumize
sealed interface NmNestSi

class NmNestHost {
    object L : NmNestSi
}
