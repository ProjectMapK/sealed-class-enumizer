package org.wrongwrong.fixtures.manualimpl

import kotlin.reflect.KClass

// 階層内の手動実装: 末端 class 自身が生成 Enumish を実装する形（docs/コンパイラプラグイン設計00.md §5.2 が継承者一覧に
// 載せる対象）。インスタンスは Enumish として振る舞うが kind ではない（kind はこの末端の companion）
data class ManualLeaf(val v: Int) : WithManual, WithManual.Enumish {
    override val label: String
        get() = "manual-value"

    override val enumizedClass: KClass<out WithManual>
        get() = ManualLeaf::class
}
