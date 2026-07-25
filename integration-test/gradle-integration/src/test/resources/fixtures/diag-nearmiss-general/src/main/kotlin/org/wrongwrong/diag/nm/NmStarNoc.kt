package org.wrongwrong.diag.nm

import org.wrongwrong.diag.nm.far.*

// TC-DIAG-113: 別パッケージの typealias を star import した末端（companion 無し）でも自動生成される
class NmStarNoc(val v: Int) : NmFarAlias
