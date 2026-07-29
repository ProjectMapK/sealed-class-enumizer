package io.github.projectmapk.mpp.fixtures

import io.github.projectmapk.sealedClassEnumizer.label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// 末端 fun interface の SAM が全ターゲットで 1 メソッドに保たれることの box テスト
// （docs/test/ケース05-境界横断.md XMP-34・V10-c）。ラムダで Handler.Fn を生成できること自体が
// 「生成 asEnumish が default 実装で SAM を壊していない」ことの実証になる
class SamTest {
    // SAM ラムダ生成が成立し、抽象メソッドは handle の 1 つに保たれている
    @Test
    fun samConversionStillWorksOnLeafFunInterface() {
        val fn = Handler.Fn { "ran" }
        assertEquals("ran", fn.handle())
    }

    // ラムダ由来のインスタンスも Fn の kind（companion）に吸収される
    @Test
    fun lambdaInstanceIsAbsorbedIntoLeafKind() {
        val fn: Handler = Handler.Fn { "x" }
        assertSame(Handler.Fn.Companion, fn.asEnumish())
        assertEquals("Fn", fn.label)
    }

    // entries は末端の数だけ（ラムダ実装で増えない）
    @Test
    fun entriesListLeavesOnly() {
        assertEquals(listOf("Direct", "Fn"), Handler.Enumish.entries.map { it.label })
    }
}
