package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumish
import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-51: 型引数一致の手動宣言（直接・間接・末端冗長宣言）は注入スキップで
// MANUAL_SUPERTYPE_MISMATCH / SUPERTYPE_APPEARS_TWICE とも非発火

// 基底が runtime-api の基底 Enumish を手動継承
@Enumize
sealed interface NmBase : Enumish {
    data object L : NmBase
}

// 手動 Enumized<自身の Enumish>（型引数一致）
@Enumize
sealed interface NmSelf : Enumized<NmSelf.Enumish> {
    data object L : NmSelf
}

// 型引数一致の生成 Enumish supertype 手動重複宣言（末端側）
@Enumize
sealed interface NmDup {
    data object L : NmDup, NmDup.Enumish
}

// 自作 interface 経由の間接一致（スキップ推移化の回帰）
interface OkIndBase : Enumized<OkIndSi.Enumish>

@Enumize
sealed interface OkIndSi : OkIndBase {
    data object L : OkIndSi
}
