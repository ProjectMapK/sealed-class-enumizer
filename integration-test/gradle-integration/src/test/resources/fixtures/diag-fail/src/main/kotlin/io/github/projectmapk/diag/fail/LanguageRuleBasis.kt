package io.github.projectmapk.diag.fail

// docs/test/ケース04-診断.md DIA-63: 可視性規則の言語根拠 2 件を手書き模擬で固定する
// （生成メンバーを internal 化できないこと・規則 2 フォールバックが必要であること）

// internal companion を持つ public クラス
class Expose {
    internal companion object
}

// public 関数が internal companion 型を返す → EXPOSED_FUNCTION_RETURN_TYPE（規則 2 フォールバックの根拠）
fun exposeReturn(): Expose.Companion = Expose

// public メンバーを持つ継承元
interface WeakBase {
    val v: Int
}

// override 可視性の縮小 → CANNOT_WEAKEN_ACCESS_PRIVILEGE（生成メンバーの internal 化が不成立である言語根拠）
class WeakImpl : WeakBase {
    internal override val v: Int = 1
}
