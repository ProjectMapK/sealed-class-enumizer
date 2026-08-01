package io.github.projectmapk.diag.ok.rawtracking

import io.github.projectmapk.diag.ok.rawtracking.far.NmFarAlias

// docs/test/ケース04-診断.md DIA-32: 別 pkg typealias を明示 import した末端（companion 無し）→ 成立
class NmFarNoc(val v: Int) : NmFarAlias
