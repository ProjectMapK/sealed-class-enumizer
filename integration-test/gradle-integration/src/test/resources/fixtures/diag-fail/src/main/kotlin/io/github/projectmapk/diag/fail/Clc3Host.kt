package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-29: companion が「別階層の」末端でも発火（判定は階層不問）
class Clc3Host(val v: Int) : Clc3SiA {
    companion object : Clc3SiB
}
