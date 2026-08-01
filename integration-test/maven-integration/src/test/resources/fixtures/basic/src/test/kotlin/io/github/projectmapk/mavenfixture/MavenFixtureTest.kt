package io.github.projectmapk.mavenfixture

import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

// 生成 API の実行時観測。label は設定で変わるため OUT 行として出し（照合は maven-integration 側）、
// 設定に依らない不変（valueOf が entries と同じ kind を返すこと）はここで表明する
class MavenFixtureTest {
    @Test
    fun observeGeneratedApi() {
        println("OUT: si=" + Si.Enumish.entries.joinToString(",") { it.label })
        println("OUT: pinned=" + Pinned.Enumish.entries.joinToString(",") { it.label })
        println("OUT: testOnly=" + TestOnly.Enumish.entries.joinToString(",") { it.label })
        Si.Enumish.entries.forEach { assertSame(it, Si.Enumish.valueOf(it.label)) }
    }
}
