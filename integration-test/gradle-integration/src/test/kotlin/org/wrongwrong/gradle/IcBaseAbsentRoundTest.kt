package org.wrongwrong.gradle

import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// 基底不在ラウンドのシナリオ（設計00 §5.4・§9-4、docs/テストケース管理.md TC-IC-039/040・TC-ORD-050）:
// 多ファイル sealed 階層 × プラグイン生成コード × IC の再現形。
//
// Kotlin 2.4.20-Beta1 + Gradle 9.5.0 での実測:
// - 既存 kind を宣言するファイルの連続編集（コメント・private メンバー・基底編集）はクラッシュせず、
//   各ラウンドで entries は stale にならない（R1〜R4）。生成 EntriesHolder が各 kind を参照するため、
//   これらの編集では基底ファイルが同一ラウンドへ共連れされる
// - **末端の「新規ファイル」追加はクラッシュする**（R5）。新規ファイルは前ビルドの参照グラフに無く
//   基底がラウンドに入らないため、プラグインの IR 生成が走らず、FIR 生成宣言（asEnumish）へ
//   ボディが充填されないまま codegen が「Function has no body」で失敗する。再試行でも失敗が持続し、
//   clean で回復する
// R5 はこのクラッシュを expected として固定する回帰ゲートであり、プラグイン側の修正でクラッシュしなく
// なったとき、ここが破れて検出される
class IcBaseAbsentRoundTest {
    private val siFile = "src/main/kotlin/org/wrongwrong/baseabsent/Si.kt"
    private val leafAFile = "src/main/kotlin/org/wrongwrong/baseabsent/LeafA.kt"
    private val leafBFile = "src/main/kotlin/org/wrongwrong/baseabsent/LeafB.kt"
    private val leafDFile = "src/main/kotlin/org/wrongwrong/baseabsent/LeafD.kt"
    private val useFile = "src/main/kotlin/org/wrongwrong/baseabsent/Use.kt"
    private val generatedPrefix = "org/wrongwrong/baseabsent/SI\$"
    private val expectedOut = listOf("ENTRIES=LeafA,LeafB,LeafC", "DESCRIBE=a,b,c")
    private val expectedOutWithD = listOf("ENTRIES=LeafA,LeafB,LeafC,LeafD", "DESCRIBE=a,b,c")

    @Test
    fun consecutiveEditsStayGreenAndNewLeafFileCrashesAsKnownIssue() {
        val dir = IcTestSupport.prepare("ic-base-absent", "icabs-")
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        val generated0 = generatedDigests(dir)

        // R1: 末端 class のコメントのみ編集（ABI 不変）
        TestKitHarness.replaceInFile(
            dir, leafAFile,
            "// 末端 class（companion はプラグイン自動生成）。連続編集ラウンドの主対象",
            "// 末端 class（companion はプラグイン自動生成）。連続編集ラウンドの主対象（R1 編集）",
        )
        assertRoundIsGreen(dir, generated0)

        // R2: 同じ末端へ private メンバーを追加（ABI 不変の宣言追加）
        TestKitHarness.replaceInFile(
            dir, leafAFile,
            "class LeafA(val v: Int) : SI",
            "class LeafA(val v: Int) : SI {\n    // R2: private メンバー追加\n    private fun r2(): Int = v\n}",
        )
        assertRoundIsGreen(dir, generated0)

        // R3: 別の末端ファイルを編集（連続ラウンドの編集対象を切り替える）
        TestKitHarness.replaceInFile(dir, leafBFile, "// 末端 object", "// 末端 object（R3 編集）")
        assertRoundIsGreen(dir, generated0)

        // R4: 基底ファイルを編集
        TestKitHarness.replaceInFile(
            dir, siFile,
            "// 多ファイル sealed 階層の基底（連続編集ラウンドで毎回共連れ再コンパイルされる）",
            "// 多ファイル sealed 階層の基底（連続編集ラウンドで毎回共連れ再コンパイルされる。R4 編集）",
        )
        assertRoundIsGreen(dir, generated0)

        // R5: 末端の「新規ファイル」追加 — 基底不在ラウンドで再現する既知のバックエンドクラッシュ。
        // when の枝を同時に足すのは R6（clean 回復）を網羅性エラーにしないためである。基底不在ラウンドの
        // 網羅性判定は前ラウンドのメタデータ由来の継承者一覧で行われ、枝の有無に依らず通る（修正方針案 #12）
        TestKitHarness.writeFile(
            dir, leafDFile,
            "package org.wrongwrong.baseabsent\n\n// R5: 新規ファイルで追加される末端（クラッシュ再現の引き金）\ndata object LeafD : SI\n",
        )
        TestKitHarness.replaceInFile(dir, useFile, "    LeafB -> \"b\"", "    LeafB -> \"b\"\n    LeafD -> \"d\"")
        val crash = TestKitHarness.buildAndFail(dir, "runMain")
        assertTrue(
            "Function has no body" in crash.output && "asEnumish" in crash.output,
            "既知クラッシュ（基底不在ラウンド）が再現しなくなった — docs/修正方針案.md #12 を見直すこと:\n${crash.output}",
        )

        // R5': 同一入力の再試行でも失敗が持続する（IC 状態は自己回復しない）
        val retry = TestKitHarness.buildAndFail(dir, "runMain")
        assertTrue("Function has no body" in retry.output, "再試行でも失敗が持続すること:\n${retry.output}")

        // R6: clean を挟めば同じソースが成功する（クラッシュは IC 経路に限られる）
        assertEquals(expectedOutWithD, IcTestSupport.outLines(TestKitHarness.build(dir, "clean", "runMain")))

        // R7: 末端ファイルの削除は IC 直行で成立し、基準状態の生成物とバイト一致まで復帰する
        TestKitHarness.deleteFile(dir, leafDFile)
        TestKitHarness.replaceInFile(dir, useFile, "    LeafB -> \"b\"\n    LeafD -> \"d\"", "    LeafB -> \"b\"")
        assertRoundIsGreen(dir, generated0)
    }

    // 第 2 の再現形（TC-IC-060/025 の実測 NG）: 多段中間 sealed チェーン（CSI←Mid1←Mid2←Leaf 各別
    // ファイル）で「中間 sealed ファイルのみ」を ABI 非変更編集すると、IC ラウンドに基底ファイルが
    // 含まれないまま末端が再コンパイルされ、同じ「Function has no body」ICE になる（中間 sealed は
    // 生成物から参照されないため、基底を共連れさせる依存辺が無い）。
    // clean 経由なら同一ソースが成功する（クラッシュは IC 経路限定）ことも併せて固定する
    @Test
    fun midChainOnlyEditCrashesAsKnownIssue() {
        val dir = IcTestSupport.prepare("ic-chain", "icabschain-")
        val expected = listOf("ENTRIES=Leaf", "KIND=Leaf")
        assertEquals(expected, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))

        TestKitHarness.replaceInFile(
            dir, "src/main/kotlin/org/wrongwrong/chain/Mid2.kt",
            "// チェーン 2 段目の中間 sealed（TC-IC-060 の編集対象）",
            "// チェーン 2 段目の中間 sealed（TC-IC-060 の編集対象。編集ラウンド）",
        )
        val crash = TestKitHarness.buildAndFail(dir, "runMain")
        assertTrue(
            "Function has no body" in crash.output && "asEnumish" in crash.output,
            "既知クラッシュ（中間 sealed 単独編集）が再現しなくなった — NG 記録を見直すこと:\n${crash.output}",
        )

        // clean を挟めば同じソースが成功し、実行時結果も不変
        assertEquals(expected, IcTestSupport.outLines(TestKitHarness.build(dir, "clean", "runMain")))
    }

    // 各ラウンド: ビルドが成功し（診断の偽陽性なし = TC-IC-059）、entries が stale にならず、
    // Si.kt 帰属の生成物（EntriesHolder を含む）が clean 基準とバイト一致する（TC-ORD-050）
    private fun assertRoundIsGreen(dir: Path, generated0: Map<String, String>) {
        assertEquals(expectedOut, IcTestSupport.outLines(TestKitHarness.build(dir, "runMain")))
        assertEquals(generated0, generatedDigests(dir))
    }

    private fun generatedDigests(dir: Path): Map<String, String> =
        IcTestSupport.classDigests(dir).filterKeys { it.startsWith(generatedPrefix) }
}
