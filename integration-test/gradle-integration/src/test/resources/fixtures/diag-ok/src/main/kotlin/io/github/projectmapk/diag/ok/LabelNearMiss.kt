package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-36: kind companion 名・中間名・enum 定数名・階層外サブタイプ名は
// label 判定に関与せず LABEL_CLASH は発火しない

// enum 定数名（Solo）は判定非関与 → 末端 Solo と衝突しない
@Enumize
sealed interface NmEc {
    enum class Pack : NmEc {
        Solo,
    }

    data object Solo : NmEc
}

// kind を担うだけの named companion の同名（Factory）→ 非発火
@Enumize
sealed interface NmFac {
    class FooL(val v: Int) : NmFac {
        companion object Factory
    }

    class BarL(val v: Int) : NmFac {
        companion object Factory
    }
}

// 末端の単純名が中間 sealed の名前と一致 → 非発火（中間は label を持たない）
@Enumize
sealed interface NmMid {
    sealed interface Same : NmMid
}

class NmMidOuter {
    data object Same : NmMid
}

// 階層外サブタイプの単純名（DupN）は判定非関与 → 非発火
@Enumize
sealed interface NmSubBase {
    abstract class PolyN : NmSubBase {
        companion object
    }

    data object DupN : NmSubBase
}

class NmSubHost {
    class DupN : NmSubBase.PolyN()
}
