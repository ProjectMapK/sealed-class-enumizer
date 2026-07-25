package org.wrongwrong.gradle

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

// 決定性検証（docs/コンパイラプラグイン設計00.md §9-3・docs/コンパイラプラグイン設計02.md §6・docs/テストケース管理.md TC-IC-031〜034 など）:
// (a) clean、(b) 無編集の再ビルド、(c) clean 後の from-cache 復元、(d) 別ディレクトリへの relocated
// ビルド、(e) 宣言順の入れ替え編集、の生成 .class バイト一致と実行時 OUT: 行の一致を検証する。
// フィクスチャは順序境界（中間 sealed の入れ子展開・ネスト/トップレベル/可視性の混在）と
// toString の 2 原則・valueOf 失敗メッセージを 1 階層へ集約している
class DeterminismTest {
    private val fixtureName = "determinism"
    private val sFile = "src/main/kotlin/org/wrongwrong/det/S.kt"

    // プラグイン生成物（S$Enumish / その Companion / DefaultImpls / $EntriesHolder）だけを指す接頭辞。
    // 利用者宣言のネストクラス（S$Aaa 等）は宣言順の入れ替えで LineNumberTable が変わり
    // バイトが揺れるため、宣言順非依存の主張（docs/コンパイラプラグイン設計02.md §6 規則 1）は生成物側にのみ適用する
    private val generatedPrefix = "org/wrongwrong/det/S\$Enumish"

    // 実行時基準値。ENTRIES は FQN 序数順の途中に Mid の入れ子展開（Bbb）が挟まる形
    // （docs/概要.md §5。可視性違い Inn / Priv も並びへ通常どおり算入される = TC-ORD-059）
    private val expectedOut =
        listOf(
            "ENTRIES=Nested,Inherited,Inn,Bbb,PlainObj,Aaa,Custom,Priv,Zzz",
            "TOSTR=PlainObj,parent,custom!,Aaa",
            "NOLABEL=IAE:No enumish entry with label 'X' in S",
        )

    private val aaaBlock =
        "    // 2 原則-c: data object は言語合成の toString を保つ（生成しない）\n" + "    data object Aaa : S"

    private val customBlock =
        "    // 2 原則-a: kind（companion）の手動 toString には生成しない\n" +
            "    data class Custom(val raw: String) : S {\n" +
            "        companion object {\n" +
            "            override fun toString(): String = \"custom!\"\n" +
            "        }\n" +
            "    }"

    // (a)(b)(c)(e): clean 基準 → 無編集再ビルド（UP-TO-DATE）→ clean 後の FROM-CACHE 復元 →
    // 宣言順入れ替え後の incremental、のすべてで生成物バイトと実行時挙動が一致する
    // （TC-IC-031/032・TC-ORD-006/023/028/065、TC-IC-036/037 の toString・失敗メッセージ決定性を含む）
    @Test
    fun cleanIncrementalFromCacheAndDeclarationReorderProduceIdenticalOutputs() {
        val dir = IcTestSupport.prepare(fixtureName, "det1-")
        val first = TestKitHarness.build(dir, "runMain")
        assertEquals(expectedOut, IcTestSupport.outLines(first))
        val digests0 = IcTestSupport.classDigests(dir)

        // (b) 無編集の再ビルド: コンパイルは走らず出力不変
        val second = TestKitHarness.build(dir, "runMain")
        assertEquals(TaskOutcome.UP_TO_DATE, second.task(":compileKotlin")?.outcome)
        assertEquals(expectedOut, IcTestSupport.outLines(second))
        assertEquals(digests0, IcTestSupport.classDigests(dir))

        // (c) clean 後の再ビルド: build cache から復元され、復元物がバイト一致
        TestKitHarness.build(dir, "clean")
        val third = TestKitHarness.build(dir, "runMain")
        assertEquals(TaskOutcome.FROM_CACHE, third.task(":compileKotlin")?.outcome)
        assertEquals(expectedOut, IcTestSupport.outLines(third))
        assertEquals(digests0, IcTestSupport.classDigests(dir))

        // (e) 宣言順の入れ替え（ORD-006/023/065）: entries の並びも生成物のバイトも変わらない
        TestKitHarness.replaceInFile(
            dir,
            sFile,
            "$aaaBlock\n\n$customBlock",
            "$customBlock\n\n$aaaBlock",
        )
        val fourth = TestKitHarness.build(dir, "runMain")
        assertEquals(expectedOut, IcTestSupport.outLines(fourth))
        assertEquals(
            digests0.filterKeys { it.startsWith(generatedPrefix) },
            IcTestSupport.classDigests(dir).filterKeys { it.startsWith(generatedPrefix) },
        )
    }

    // (d) relocated ビルド（TC-IC-033・TC-ORD-029）: 同一内容を別の絶対パスへ複製したビルドが
    // relocatable なキャッシュヒットになり、生成物が元ディレクトリとバイト一致する
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

    // kind の toString の生成条件が別ファイルの supertype の toString 増減に依存する（TC-IC-049・V11）:
    // 階層外の親クラス WithToString の具象 toString を除去すると 2 原則-2 により kind へ生成が入り
    // （表示が label へ）、復元すると生成が止まる（継承採用へ戻る）。各状態で実行時挙動が決定的
    @Test
    fun case49ToStringGenerationFollowsSupertypeAcrossFiles() {
        val dir = IcTestSupport.prepare(fixtureName, "det49-")
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
                "ENTRIES=Nested,Inherited,Inn,Bbb,PlainObj,Aaa,Custom,Priv,Zzz",
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

    // 同一ファイルに 2 階層（TC-IC-062）: 片方の階層の編集で再コンパイル単位（ファイル）ごと
    // 両階層が再生成されるが、未編集側 SB の生成物はバイト一致のまま（P1/P2 = 継承者集合非依存）。
    // L-41（別ファイル配置では非共連れ）の対極となる file-granularity 境界
    @Test
    fun case62TwoHierarchiesInOneFileRegenerateTogetherButKeepBytes() {
        val dir = IcTestSupport.prepare("ic-shared-file", "det62-")
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
}
