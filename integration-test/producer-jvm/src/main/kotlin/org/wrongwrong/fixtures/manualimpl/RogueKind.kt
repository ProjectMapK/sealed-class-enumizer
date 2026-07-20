package org.wrongwrong.fixtures.manualimpl

import kotlin.reflect.KClass

// 生成 Enumish（sealed）の手動実装。sealed の言語制約により同一モジュール・同一パッケージに置く。
// entries / valueOf の保証対象は kind に閉じており、この値はどちらにも現れない（docs/概要.md §8 の許容）
object RogueKind : WithManual.Enumish {
    override val label: String get() = "Rogue"

    override val enumizedClass: KClass<out WithManual> get() = WithManual.Real::class
}
