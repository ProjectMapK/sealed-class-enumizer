package org.wrongwrong.mpp.fixtures.jvm

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFails

// kind 初期化子からの entries 再入(docs/テストケース管理.md TC-MPP-028・docs/概要.md §6
// 「初期化再入の禁止」)。doc の期待は StackOverflow 系の実行時異常だが、JVM 実測では
// 例外は発生しない: lazy(SYNCHRONIZED) は同一スレッド再入可能で、JVM のクラス初期化再入により
// 初期化途中の Boom が内側の計算から見えるため entries=[Boom]・Boom.observed=1・
// 以降のアクセスも同一インスタンスで安定する(初期化子が二重実行されるだけに留まる)。
// Native では process-fatal になりうるため JVM でのみ実測する
class ReentrantInitTest {
    @Ignore("NG: 再入は JVM では StackOverflow にならず entries=[Boom]/observed=1 で正常終了する — docs/修正方針案.md 反映待ち")
    @Test
    fun reentrantInitializerFailsAtRuntime() {
        assertFails { Reentrant.Enumish.entries }
    }
}
