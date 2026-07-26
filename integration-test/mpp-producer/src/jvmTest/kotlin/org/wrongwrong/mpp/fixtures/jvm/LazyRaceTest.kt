package org.wrongwrong.mpp.fixtures.jvm

import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertSame

// JVM 実マルチスレッドでの entries 初回アクセス競合
// （docs/test/ケース01-生成と実行時API.md API-47・docs/test/ケース05-境界横断.md XMP-46）。
// lazy(SYNCHRONIZED) により createEntries は一度だけ実行され、全スレッドが同一インスタンスを得る。
// 初回アクセスを競わせるため Raced 階層はこのテストでのみ参照する
class LazyRaceTest {
    @Test
    fun concurrentFirstAccessYieldsSingleList() {
        val workers = 8
        val barrier = CyclicBarrier(workers)
        val seen = arrayOfNulls<List<Raced.Enumish>>(workers)
        val threads =
            (0 until workers).map { index ->
                thread {
                    barrier.await()
                    seen[index] = Raced.Enumish.entries
                }
            }
        threads.forEach { it.join() }
        seen.forEach { assertSame(seen[0], it) }
    }
}
