package io.github.projectmapk.diag.ok

// docs/test/ケース04-診断.md DIA-70: open 具象 label の直接継承末端 object は非発火（生成 override が勝つ）
object OkCiOpen : OkCiBase(), OkCiSi
