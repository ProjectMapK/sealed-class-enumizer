package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-63: override 可視性の縮小 → CANNOT_WEAKEN_ACCESS_PRIVILEGE
// （生成メンバーの internal 化が不成立である言語根拠）
class WeakImpl : WeakBase {
    internal override val v: Int = 1
}
