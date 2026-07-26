package org.wrongwrong.diag.ok

// docs/test/ケース04-診断.md DIA-44: kind が継承経路上の具象 toString を持つ → MC 非発火（toString は対象外）
object NmTsLeaf : NmTsBase(), NmTs
