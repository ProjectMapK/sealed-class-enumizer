package io.github.projectmapk.gradle

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes
import kotlin.streams.asSequence
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

// MPP（klib を含む）のビルド動態（docs/test/ケース06-ビルド動態.md BLD-42〜44・
// docs/test/ケース05-境界横断.md XMP-31）。
// KGP は MPP のコンパイルを既定で非 incremental にする（KT-62686）ため、commonMain の編集ラウンドには
// 常に階層の全ファイルが入り、基底不在ラウンドは発生しない
class MppIcTest {
    private val tasks =
        arrayOf(
            "runMain",
            "compileKotlinJs",
            "compileKotlinWasmJs",
            "compileCommonMainKotlinMetadata",
        )
    private val outputDir = "build/classes/kotlin"
    private val metadataDir = "build/classes/kotlin/metadata"
    private val leafCFile = "src/commonMain/kotlin/io/github/projectmapk/icmpp/LeafC.kt"
    private val useFile = "src/commonMain/kotlin/io/github/projectmapk/icmpp/Use.kt"
    private val leafCSource =
        "package io.github.projectmapk.icmpp\n\n// 新規ファイルで追加される末端\ndata object LeafC : SI\n"
    private val baselineOut = listOf("ENTRIES=LeafA,LeafB", "DESCRIBE=a,b")
    private val addedOut = listOf("ENTRIES=LeafA,LeafB,LeafC", "DESCRIBE=a,b")

    // docs/test/ケース06-ビルド動態.md BLD-42: 末端追加が jvm / js / wasmJs / metadata の全成果物
    // （klib 含む）へ反映し、同一ソースの clean ビルドと全一致する（V8）
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

    // docs/test/ケース06-ビルド動態.md BLD-43: 末端追加後、metadata コンパイル単独で
    // else 無し kind-when が非網羅エラーになる
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

    // docs/test/ケース05-境界横断.md XMP-31: metadata klib の内容検査 = 生成宣言（Enumish）の搭載・
    // inheritors の FQN 順直列化・IR-only の $EntriesHolder 非搭載（V5 / V2-b）
    @Test
    fun metadataKlibCarriesDeclarationsWithoutEntriesHolder() {
        val dir = IcTestSupport.prepare("ic-mpp", "icmppmeta-")
        TestKitHarness.build(dir, "compileCommonMainKotlinMetadata")
        val contents = metadataFileContents(dir)
        assertTrue(contents.isNotEmpty(), "metadata 出力が存在すること")

        assertTrue(
            contents.values.any { "Enumish" in it },
            "生成宣言（Enumish）が metadata へ直列化されること: ${contents.keys}",
        )
        assertTrue(
            contents.values.none { "EntriesHolder" in it },
            "IR-only の \$EntriesHolder は metadata に載らないこと",
        )
        // inheritors の FQN 順直列化: LeafA / LeafB を併載するファイル内で LeafA が先に現れる
        val withLeaves = contents.values.filter { "LeafA" in it && "LeafB" in it }
        assertTrue(withLeaves.isNotEmpty(), "両末端を併載する metadata ファイルが存在すること")
        assertTrue(
            withLeaves.all { it.indexOf("LeafA") < it.indexOf("LeafB") },
            "inheritors が FQN 順で直列化されること",
        )
    }

    // docs/test/ケース06-ビルド動態.md BLD-44: MPP 全ターゲットの from-cache 復元・relocated ビルドが
    // clean とバイト一致する（V8）
    @Test
    fun mppFromCacheAndRelocatedBuildsMatchBytes() {
        val dirA = IcTestSupport.prepare("ic-mpp", "icmppcA-")
        assertEquals(baselineOut, IcTestSupport.outLines(TestKitHarness.build(dirA, *tasks)))
        val digestsA = IcTestSupport.outputDigests(dirA, outputDir)

        // clean 後の再ビルド: build cache から復元され、復元物がバイト一致
        TestKitHarness.build(dirA, "clean")
        val restored = TestKitHarness.build(dirA, *tasks)
        assertEquals(TaskOutcome.FROM_CACHE, restored.task(":compileKotlinJvm")?.outcome)
        assertEquals(digestsA, IcTestSupport.outputDigests(dirA, outputDir))

        // relocated: 別絶対パスの複製ビルドでも全成果物がバイト一致
        val dirB = IcTestSupport.emptyDir("icmppcB-")
        IcTestSupport.copyForRelocation(dirA, dirB)
        TestKitHarness.build(dirB, *tasks)
        assertEquals(digestsA, IcTestSupport.outputDigests(dirB, outputDir))
    }

    // metadata 出力配下の全ファイルをバイト列文字列（ISO_8859_1）として読む（内容検査用）
    private fun metadataFileContents(dir: Path): Map<String, String> {
        val root = dir.resolve(metadataDir)
        if (!Files.isDirectory(root)) return emptyMap()
        return Files.walk(root).use { paths ->
            paths
                .asSequence()
                .filter { it.isRegularFile() }
                .associate { it.toString() to String(it.readBytes(), Charsets.ISO_8859_1) }
        }
    }
}
