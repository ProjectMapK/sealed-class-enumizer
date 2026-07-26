package org.wrongwrong.gradle

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

// 決定性検証（docs/test/ケース06-ビルド動態.md BLD-02〜06・BLD-32/33）:
// clean / UP-TO-DATE / from-cache / relocated / 宣言順（中間 sealed 内部の並べ替え・
// 階層内手動実装込み = inheritors 正規化）/ ロケールのバイト一致と、toString の別ファイル依存編集。
// determinism フィクスチャは順序境界 + toString 2 原則 + 手動実装を 1 階層へ集約し、
// file-granularity 境界（BLD-32）は ic-shared-file が担う
class DeterminismTest {
    private val fixtureName = "determinism"
    private val sFile = "src/main/kotlin/org/wrongwrong/det/S.kt"
    private val midFile = "src/main/kotlin/org/wrongwrong/det/Mid.kt"

    // プラグイン生成物（S$Enumish 一式）だけを指す接頭辞。利用者宣言のネストクラスは
    // 宣言順の入れ替えで LineNumberTable が変わりバイトが揺れるため、宣言順非依存の主張
    // （docs/コンパイラプラグイン設計02.md §6 規則 1）は生成物側にのみ適用する
    private val generatedPrefix = "org/wrongwrong/det/S\$Enumish"

    // 実行時基準値。ENTRIES は FQN 序数順の途中に Mid の入れ子展開（Bbb, MA, MB）が挟まる形
    // （docs/概要.md §5。手動実装 ManualLeaf は kind を成さず entries に載らない）
    private val expectedOut =
        listOf(
            "ENTRIES=Nested,Inherited,Inn,ManualLeaf,Bbb,MA,MB,PlainObj,Aaa,Custom,Priv,Zzz",
            "TOSTR=PlainObj,parent,custom!,Aaa",
            "NOLABEL=IAE:No enumish entry with label 'X' in S",
        )

    private val aaaBlock =
        "    // 原則 1(c): data object は言語合成の toString を保つ（生成しない）\n" + "    data object Aaa : S"

    private val customBlock =
        "    // 原則 1(a): kind（companion）の手動 toString には生成しない\n" +
            "    data class Custom(val raw: String) : S {\n" +
            "        companion object {\n" +
            "            override fun toString(): String = \"custom!\"\n" +
            "        }\n" +
            "    }"

    // docs/test/ケース06-ビルド動態.md BLD-02/06: clean → 無編集再実行（UP_TO_DATE）→
    // キャッシュ復元（FROM_CACHE）で全生成物バイト一致（toString・kind アクセサ込み）
    @Test
    fun cleanIncrementalAndFromCacheProduceIdenticalBytes() {
        val dir = IcTestSupport.prepare(fixtureName, "det1-")
        val first = TestKitHarness.build(dir, "runMain")
        assertEquals(expectedOut, IcTestSupport.outLines(first))
        val digests0 = IcTestSupport.classDigests(dir)

        val second = TestKitHarness.build(dir, "runMain")
        assertEquals(TaskOutcome.UP_TO_DATE, second.task(":compileKotlin")?.outcome)
        assertEquals(expectedOut, IcTestSupport.outLines(second))
        assertEquals(digests0, IcTestSupport.classDigests(dir))

        TestKitHarness.build(dir, "clean")
        val third = TestKitHarness.build(dir, "runMain")
        assertEquals(TaskOutcome.FROM_CACHE, third.task(":compileKotlin")?.outcome)
        assertEquals(expectedOut, IcTestSupport.outLines(third))
        assertEquals(digests0, IcTestSupport.classDigests(dir))
    }

    // docs/test/ケース06-ビルド動態.md BLD-03: 別絶対パスへの複製ビルドが FROM_CACHE でヒットし
    // バイト一致する（relocatable キャッシュ）
    @Test
    fun relocatedBuildHitsCacheAndMatchesBytes() {
        val dirA = IcTestSupport.prepare(fixtureName, "detA-")
        val first = TestKitHarness.build(dirA, "runMain")
        assertEquals(expectedOut, IcTestSupport.outLines(first))
        val digestsA = IcTestSupport.classDigests(dirA)

        // 複製先の settings は複製元のローカルキャッシュ絶対パスを指したままのため、キャッシュは共有される
        val dirB = IcTestSupport.emptyDir("detB-")
        IcTestSupport.copyForRelocation(dirA, dirB)
        val relocated = TestKitHarness.build(dirB, "compileKotlin")
        assertEquals(TaskOutcome.FROM_CACHE, relocated.task(":compileKotlin")?.outcome)
        assertEquals(digestsA, IcTestSupport.classDigests(dirB))
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dirB, "runMain")))
    }

    // docs/test/ケース06-ビルド動態.md BLD-04: ファイル内宣言順の入替（基底本体 + 中間 sealed 内部の
    // 並べ替え・階層内手動実装込み = inheritors の登録順非依存 FQN 正規化）で OUT・生成物バイト一致
    @Test
    fun declarationReorderKeepsBytes() {
        val dir = IcTestSupport.prepare(fixtureName, "det4-")
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val generated0 =
            IcTestSupport.classDigests(dir).filterKeys { it.startsWith(generatedPrefix) }

        // 基底本体内の宣言順入替
        TestKitHarness.replaceInFile(
            dir,
            sFile,
            "$aaaBlock\n\n$customBlock",
            "$customBlock\n\n$aaaBlock",
        )
        // 中間 sealed 内部の並べ替え
        TestKitHarness.replaceInFile(
            dir,
            midFile,
            "    data object MA : Mid\n\n    data object MB : Mid",
            "    data object MB : Mid\n\n    data object MA : Mid",
        )
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(
            generated0,
            IcTestSupport.classDigests(dir).filterKeys { it.startsWith(generatedPrefix) },
        )
    }

    // docs/test/ケース06-ビルド動態.md BLD-05: 既定ロケールと tr-TR の clean 出力がバイト一致
    // （collator・'I'/'i' 特殊変換の非混入。順序はコンパイル時に確定する）
    @Test
    fun turkishLocaleBuildMatchesDefaultLocaleBytes() {
        val defaultDir = IcTestSupport.prepare(fixtureName, "detloc1-")
        val turkishDir = IcTestSupport.prepare(fixtureName, "detloc2-")
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

    // docs/test/ケース06-ビルド動態.md BLD-32: 同一ファイル 2 階層の片側編集は両階層をファイル単位で
    // 再生成するが、未編集側の生成物はバイト一致（P3 は論理集約・物理は共連れ）
    @Test
    fun sharedFileHierarchiesRegenerateTogetherKeepBytes() {
        val dir = IcTestSupport.prepare("ic-shared-file", "det32-")
        val twoFile = "src/main/kotlin/org/wrongwrong/shared/Two.kt"
        val sbPrefix = "org/wrongwrong/shared/SB\$Enumish"
        assertEquals(
            listOf("SA=A1", "SB=B1"),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )
        val sbGenerated0 = IcTestSupport.classDigests(dir).filterKeys { it.startsWith(sbPrefix) }
        val times0 = IcTestSupport.classTimes(dir)

        // SA 側だけを編集（末端の改名 = 行数を変えない編集。行がずれると未編集側 SB の生成クラスも
        // LineNumberTable 差分でバイトが揺れるため、バイト一致の主張は行数保存編集で検証する）
        TestKitHarness.replaceInFile(
            dir,
            twoFile,
            "    data object A1 : SA",
            "    data object A9 : SA",
        )
        assertEquals(
            listOf("SA=A9", "SB=B1"),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )
        val changed = IcTestSupport.changedKeys(times0, IcTestSupport.classTimes(dir))
        assertTrue(changed.any { it.startsWith(sbPrefix) }, "未編集側もファイル単位で再生成されること: $changed")
        assertEquals(
            sbGenerated0,
            IcTestSupport.classDigests(dir).filterKeys { it.startsWith(sbPrefix) },
        )
    }

    // docs/test/ケース06-ビルド動態.md BLD-33: 階層外親の具象 toString 除去 / 復元で kind の表示が
    // label ⇔ 継承表示へ追随する（原則 2 の跨ファイル判定）
    @Test
    fun toStringFollowsSupertypeEditAcrossFiles() {
        val dir = IcTestSupport.prepare(fixtureName, "det33-")
        val withToStringFile = "src/main/kotlin/org/wrongwrong/det/WithToString.kt"
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))

        // 親クラスの具象 toString を除去 → Inherited の kind へ label を返す toString が生成される
        TestKitHarness.replaceInFile(
            dir,
            withToStringFile,
            "    override fun toString(): String = \"parent\"",
            "    fun placeholderNote(): Int = 1",
        )
        assertEquals(
            listOf(
                "ENTRIES=Nested,Inherited,Inn,ManualLeaf,Bbb,MA,MB,PlainObj,Aaa,Custom,Priv,Zzz",
                "TOSTR=PlainObj,Inherited,custom!,Aaa",
                "NOLABEL=IAE:No enumish entry with label 'X' in S",
            ),
            IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")),
        )

        // 復元 → 継承採用（生成しない）へ戻り、基準値と一致する
        TestKitHarness.replaceInFile(
            dir,
            withToStringFile,
            "    fun placeholderNote(): Int = 1",
            "    override fun toString(): String = \"parent\"",
        )
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
    }
}
