package org.wrongwrong.diag.nm

import org.wrongwrong.diag.nm.far.NmFarAlias

// TC-DIAG-112: 別パッケージの typealias を明示 import した末端（companion 無し）でも自動生成される
class NmFarNoc(val v: Int) : NmFarAlias
