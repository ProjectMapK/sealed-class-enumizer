package org.wrongwrong.diag.ok

// docs/test/ケース04-診断.md DIA-19: inner class が非 final 末端のサブタイプ → 吸収され AK / IL 非発火
class NmInHost {
    inner class Tri : NmIn.Poly()
}
