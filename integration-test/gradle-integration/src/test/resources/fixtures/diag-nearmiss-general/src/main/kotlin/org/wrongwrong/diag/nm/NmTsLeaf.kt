package org.wrongwrong.diag.nm

// TC-DIAG-083: kind が継承経路上の具象 toString を持つ → 非発火（toString は生成対象外・原則 1(b)）
object NmTsLeaf : NmTsBase(), NmTs
