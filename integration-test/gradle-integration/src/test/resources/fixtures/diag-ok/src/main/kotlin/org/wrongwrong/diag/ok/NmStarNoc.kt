package org.wrongwrong.diag.ok

import org.wrongwrong.diag.ok.far.*

// docs/test/ケース04-診断.md DIA-32: 別 pkg typealias を star import した末端（companion 無し）→ 成立
class NmStarNoc(val v: Int) : NmFarAlias
