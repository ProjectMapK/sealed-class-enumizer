package org.wrongwrong.gradle

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

// MPP（klib を含む）の増分ビルド（docs/テストケース管理.md TC-IC-069 = V8 の klib IC）。
// KGP は MPP のコンパイルを既定で非 incremental にする（ビルドレポートが
// `REBUILD_REASON: Incremental compilation might be incorrect (KT-62686)` を報告する）ため、
// commonMain の編集ラウンドには常に階層の全ファイルが入り、基底不在ラウンドは発生しない。
// ここでは新規ファイルでの末端追加が jvm / js / wasmJs / metadata の全コンパイルへ反映され、
// 成果物が同一ソースの clean ビルドとバイト一致することを固定する
class MppIcTest {
    private val tasks =
        arrayOf(
            "runMain",
            "compileKotlinJs",
            "compileKotlinWasmJs",
            "compileCommonMainKotlinMetadata",
        )
    private val outputDir = "build/classes/kotlin"
    private val leafCFile = "src/commonMain/kotlin/org/wrongwrong/icmpp/LeafC.kt"
    private val useFile = "src/commonMain/kotlin/org/wrongwrong/icmpp/Use.kt"
    private val leafCSource =
        "package org.wrongwrong.icmpp\n\n// 新規ファイルで追加される末端\ndata object LeafC : SI\n"
    private val baselineOut = listOf("ENTRIES=LeafA,LeafB", "DESCRIBE=a,b")
    private val addedOut = listOf("ENTRIES=LeafA,LeafB,LeafC", "DESCRIBE=a,b")

    @Test
    fun leafAdditionReachesAllTargetsAndMatchesCleanBuild() {
        val dir = IcTestSupport.prepare("ic-mpp", "icmpp-")
        assertEquals(baselineOut, IcTestSupport.outLines(TestKitHarness.build(dir, *tasks)))

        TestKitHarness.writeFile(dir, leafCFile, leafCSource)
        TestKitHarness.replaceInFile(
            dir,
            useFile,
            "    LeafB -> \"b\"",
            "    LeafB -> \"b\"\n    LeafC -> \"c\"",
        )
        assertEquals(addedOut, IcTestSupport.outLines(TestKitHarness.build(dir, *tasks)))
        val incremental = IcTestSupport.outputDigests(dir, outputDir)
        assertTrue(
            incremental.keys.any { it.startsWith("js/") } &&
                incremental.keys.any { it.startsWith("wasmJs/") },
            "klib 出力が観測対象に含まれること: ${incremental.keys}",
        )

        assertEquals(addedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "clean", *tasks)))
        assertEquals(
            IcTestSupport.outputDigests(dir, outputDir),
            incremental,
            "klib / metadata / jvm の全成果物が clean と一致すること",
        )
    }

    // 追加した末端は commonMain の kind-when を非網羅にする（診断は metadata / platform の
    // どちらのコンパイルでも同じ条件検査として発火する）
    @Test
    fun leafAdditionBreaksCommonKindWhenExhaustiveness() {
        val dir = IcTestSupport.prepare("ic-mpp", "icmppdiag-")
        assertEquals(baselineOut, IcTestSupport.outLines(TestKitHarness.build(dir, *tasks)))

        TestKitHarness.writeFile(dir, leafCFile, leafCSource)
        val failure = TestKitHarness.buildAndFail(dir, "compileCommonMainKotlinMetadata")
        assertTrue(
            "exhaustive" in failure.output,
            "common の kind-when が非網羅になること:\n${failure.output}",
        )
    }
}
