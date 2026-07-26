package org.wrongwrong.fixtures.manual.impl

import kotlin.reflect.KClass

// 階層内の手動実装（open 形。docs/test/ケース01-生成と実行時API.md API-40）:
// 末端 class 自身が生成 Enumish を実装する形で、直接実装として inheritors に登載される
// （kind 単位の網羅 when に is 枝が必要 = API-41）。インスタンスは Enumish として振る舞うが
// kind ではない（この末端の kind は自動生成 companion）
open class ManualLeaf(val v: Int) : WithManual, WithManual.Enumish {
    override val label: String
        get() = "manual-value"

    override val enumizedClass: KClass<out WithManual>
        get() = ManualLeaf::class
}
