package org.wrongwrong.fixtures.internalleaves

import org.wrongwrong.sealedClassEnumizer.Enumize

// public 基底 + internal な各種別末端（TC-GAP-015 の生成側・TC-VIS-010）。
// 末端はファイル分散のトップレベル internal 宣言（別ファイルの internal 末端は
// ENUMIZE_KIND_NOT_ACCESSIBLE の対象外 = TC-VIS-034 の非発火側）
@Enumize
sealed interface InternalLeaves
