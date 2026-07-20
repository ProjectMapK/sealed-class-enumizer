package org.wrongwrong.mpp.fixtures.jvm

import org.wrongwrong.sealedClassEnumizer.Enumize

// kind 初期化子から entries を参照する病的構成（docs/概要.md §6「初期化再入の禁止」・
// docs/テストケース管理.md TC-MPP-028）。Native では実行時異常が process-fatal になりうるため
// jvmMain に置き、JVM でのみ実測する（このフィクスチャに触れるのは ReentrantInitTest だけである）
@Enumize
sealed interface Reentrant {
    data object Boom : Reentrant {
        // 遅延初期化への同一スレッド再入を起こす初期化子
        val observed: Int = Reentrant.Enumish.entries.size
    }
}
