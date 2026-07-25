package org.wrongwrong.gradle

import kotlin.test.Test
import kotlin.test.assertEquals

// 残ケース掃討: entries 順序のロケール非依存（docs/テストケース管理.md TC-ORD-011 の JVM 面）。
// 順序はコンパイル時に確定するため、コンパイラを走らせる JVM の既定ロケールを Turkish
// （'I'/'i' の大小変換が特殊で collator 混入の検出に適する）へ変えた 2 ビルドの出力バイトを比較する
class SweepLocaleDeterminismTest {
    // determinism フィクスチャ（'I' を含む宣言 Inherited / Inn を持つ）を既定ロケールと tr-TR で
    // 各 1 回ずつ clean コンパイルし、生成 .class 全体がバイト一致することを確認する
    @Test
    fun compilationUnderTurkishLocaleProducesIdenticalBytes() {
        val defaultDir = IcTestSupport.prepare("determinism", "swloc1-")
        val turkishDir = IcTestSupport.prepare("determinism", "swloc2-")
        val properties = IcTestSupport.readFile(turkishDir, "gradle.properties")
        TestKitHarness.writeFile(
            turkishDir,
            "gradle.properties",
            properties + "\norg.gradle.jvmargs=-Xmx1g -Duser.language=tr -Duser.country=TR\n",
        )

        TestKitHarness.build(defaultDir, "compileKotlin")
        TestKitHarness.build(turkishDir, "compileKotlin")

        assertEquals(IcTestSupport.classDigests(defaultDir), IcTestSupport.classDigests(turkishDir))
    }
}
