package org.wrongwrong.fixtures

import kotlin.test.Test
import kotlin.test.assertEquals

// 生成 Enumish の inheritors 順（kind ClassId の FQN 昇順）と entries 順（末端 ClassId の DFS 順）の
// 関係（docs/テストケース管理.md TC-ORD-052）。フラットな兄弟配置（class 末端 Foo + object 末端 Bar）
// では '.Companion' 接尾辞込みでも両者の相対順が一致することを固定する（末端内ネスト時の分岐は
// D4X-04 = 別ケースの管轄）
class SweepInheritorsOrderTest {
    // SI.Enumish::class.sealedSubclasses（inheritors 属性由来）と entries の kind 実行時クラス列が
    // 集合・並びとも一致する（SI.Bar < SI.Foo.Companion の FQN 順）
    @Test
    fun inheritorsOrderMatchesEntriesOrderInFlatSiblingLayout() {
        assertEquals(SI.Enumish.entries.map { it::class }, SI.Enumish::class.sealedSubclasses)
    }
}
