package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-15: 階層メンバー（末端・中間）自身への @Enumize は NESTED_IN_HIERARCHY

// 末端自身への付与 → NESTED + 基底 FQN・自己生成 supertype の食い違いで MSM 併発
@Enumize
sealed interface MultNested {
    @Enumize
    data object Leaf : MultNested
}

// @Enumize 付き中間は NESTED・その配下末端には MULTIPLE_HIERARCHIES
// （上向き探索は最初の基底で停止せず 2 基底へ到達する）
@Enumize
sealed interface MultNestedMid {
    @Enumize
    sealed interface Mid : MultNestedMid {
        data object MLeaf : Mid
    }
}
