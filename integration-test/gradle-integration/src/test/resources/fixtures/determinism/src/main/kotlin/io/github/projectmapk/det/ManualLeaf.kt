package io.github.projectmapk.det

import kotlin.reflect.KClass

// 階層内手動実装（docs/test/ケース06-ビルド動態.md BLD-04: inheritors の登録順非依存
// FQN 正規化の観測材料。末端としての kind = 自動生成 companion は entries に載り
// （label = "ManualLeaf"）、手動実装の値（label override = "ManualLeaf" の getter）は kind でない）
class ManualLeaf(val raw: String) : S, S.Enumish {
    override val label: String get() = "ManualLeaf"

    override val enumizedClass: KClass<out S> get() = ManualLeaf::class
}
