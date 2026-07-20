package org.wrongwrong.sweep.fail

// TC-VIS-045: public メンバーの override を internal へ狭める → CANNOT_WEAKEN_ACCESS_PRIVILEGE。
// 生成メンバー（runtime-api の public 宣言の override）を internal 化する縮退案が不成立である言語根拠
class SwWeakImpl : SwWeakBase {
    internal override val v: Int = 1
}
