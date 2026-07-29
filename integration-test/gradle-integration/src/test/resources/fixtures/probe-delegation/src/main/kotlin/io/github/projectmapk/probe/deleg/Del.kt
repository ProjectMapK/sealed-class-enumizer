package io.github.projectmapk.probe.deleg

// docs/test/ケース04-診断.md DIA-69: 基底へのクラス委譲末端。委譲が asEnumish の実装を供給し、
// 生成 override との衝突（宣言衝突）か委譲勝ち（kind 契約破れ）のどちらに倒れるかを観測する
class Del(private val o: PSi) : PSi by o
