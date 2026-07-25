package org.wrongwrong.gradle

import kotlin.test.Test
import kotlin.test.assertTrue
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsentAt

// 残ケース掃討: 軸間重複の写像先が未確定のまま残った発火系ケースを sweep-diag-fail フィクスチャ
// （複数の独立階層を 1 モジュールへ収容）へ集約して検証する。対象 TC は各メソッドのコメントに記す
// （docs/テストケース管理.md 残ケース掃討・docs/修正方針案.md の既知 NG 対応）
class SweepDiagFailTest : DiagTestBase() {
    private fun sweep(): String = failOutput("sweep-diag-fail", "compileKotlin")

    // 指定ファイルの診断行（e:/w:）に、指定断片をすべて含む行があることを検査する（行番号は問わない）
    private fun assertDiagnosticInFile(output: String, file: String, vararg fragments: String) {
        val lines =
            output
                .lineSequence()
                .filter { it.contains("$file:") && (it.contains("e: ") || it.contains("w: ")) }
                .toList()
        assertTrue(
            lines.any { candidate -> fragments.all(candidate::contains) },
            "期待した診断が $file に無い: ${fragments.toList()}\n$file の診断行: $lines",
        )
    }

    // TC-LEAF-090: 別ファイルの private 外側クラスにネストした末端は IR-only アクセサ経由で load し、
    // 診断ゼロで通る（発火し始めたら検出する回帰ゲート。実挙動は producer-jvm / mpp-producer が固定）
    @Test
    fun privateOuterNestedLeafLoadsWithoutDiagnostics() {
        val output = sweep()
        assertFragmentAbsentAt(output, "SwKnaOuter.kt", "e: ")
        assertFragmentAbsentAt(output, "SwKnaSi.kt", "e: ")
    }

    // TC-LEAF-067 / TC-MAN-032: 末端 class の既存 companion（= kind・生成先）の label 手動宣言
    @Test
    fun manualLabelOnClassCompanionConflicts() {
        assertDiagnosticInFile(sweep(), "SwClSi.kt", DiagFragments.MANUAL_MEMBER_CONFLICT)
    }

    // TC-MAN-066: 末端 enum class の companion（= kind）の label 手動宣言（enum でも同一扱い）
    @Test
    fun manualLabelOnEnumCompanionConflicts() {
        assertDiagnosticInFile(sweep(), "SwEcSi.kt", DiagFragments.MANUAL_MEMBER_CONFLICT)
    }

    // TC-MAN-063: 末端 interface の asEnumish 手動宣言（生成先 = default 実装）
    @Test
    fun manualAsEnumishOnInterfaceLeafConflicts() {
        assertDiagnosticInFile(sweep(), "SwIaSi.kt", DiagFragments.MANUAL_MEMBER_CONFLICT)
    }

    // TC-MAN-065: 階層外 interface からの asEnumish default 実装の継承（label 版 = TC-DIAG-047 の対応物）
    @Test
    fun inheritedAsEnumishDefaultConflicts() {
        assertDiagnosticInFile(sweep(), "SwMhLeaf.kt", DiagFragments.MANUAL_MEMBER_CONFLICT)
    }

    // TC-LEAF-068: 末端自身の Enumized<別の Enumish 型> 手動継承 → MANUAL_SUPERTYPE_MISMATCH
    // （基底側 = TC-DIAG-050 の末端側対応物）
    @Test
    fun leafSideEnumizedMismatchIsReported() {
        assertDiagnosticInFile(sweep(), "SwLeafMm.kt", DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
    }

    // TC-MAN-071: 末端が他階層の生成 Enumish を型引数とする Enumized を手動継承 →
    // MANUAL_SUPERTYPE_MISMATCH であり ENUMIZE_MULTIPLE_FAMILIES ではない（切り分け境界の実測固定）
    @Test
    fun crossFamilyEnumizedIsMismatchNotMultipleFamilies() {
        val output = sweep()
        assertDiagnosticInFile(output, "SwTfCross.kt", DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
        assertFragmentAbsentAt(output, "SwTfCross.kt", DiagFragments.MULTIPLE_FAMILIES)
    }

    // TC-MAN-016(2): supertype の直接型引数への射影（Enumized<out ...>）は言語が拒否し、
    // MISMATCH 照合以前に決着する
    @Test
    fun projectionInSupertypeArgumentFailsInLanguage() {
        assertDiagnosticInFile(sweep(), "SwProj.kt", "Projection")
    }

    // TC-MAN-016(1): nullable 型引数（Enumized<...Enumish?>）は out T : Enumish の境界違反として
    // 言語エラーになる（MISMATCH の管轄外）
    @Test
    fun nullableSupertypeArgumentViolatesBound() {
        assertDiagnosticInFile(sweep(), "SwNul.kt", "bound")
    }

    // TC-MAN-057(a): 同一モジュール・別パッケージからの生成 Enumish 手動実装は、生成 Enumish が
    // sealed（V1）であることの言語制約（同一パッケージ）に掛かる。プラグインの
    // ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY も併発する（階層外直接実装の現行仕様）
    @Test
    fun alienPackageManualImplementationFails() {
        assertDiagnosticInFile(
            sweep(),
            "SwAlienPkg.kt",
            DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY,
        )
    }

    // TC-VIS-045: public メンバー override の internal 化は CANNOT_WEAKEN_ACCESS_PRIVILEGE。
    // 生成メンバー（runtime-api の public 宣言の override）の internal 化縮退案が不成立である言語根拠
    @Test
    fun weakeningOverrideVisibilityFails() {
        assertDiagnosticInFile(sweep(), "SwWeakImpl.kt", "weaken")
    }

    // TC-VIS-056: public 関数が internal companion 型を返すと EXPOSED_FUNCTION_RETURN_TYPE。
    // asEnumish 返り値型の規則 2 フォールバックが回避しているエラーの手書き模擬
    @Test
    fun exposedFunctionReturnTypeFails() {
        assertDiagnosticInFile(sweep(), "SwExposeReturn.kt", "exposes")
    }

    // TC-VIS-002/007/009: private トップレベル基底・private ネスト基底・protected ネスト基底を
    // 可視範囲の外から参照 → 言語の可視性エラーのみで、プラグイン独自診断は出ない
    @Test
    fun scopeViolationsFailWithoutPluginDiagnostics() {
        val output = sweep()
        for (file in listOf("SwScopePrivUse.kt", "SwPrivHostUse.kt", "SwProtHostUse.kt")) {
            assertDiagnosticInFile(output, file, "e: ")
        }
    }

    // TC-VIS-062: internal 基底 + 基底内ネスト private 末端は、同一モジュールでも別ファイルからの
    // else 無し kind-when が網羅不成立（else 省略の判定はスコープ単位）
    @Test
    fun elseIsRequiredOutsideBaseBodyScope() {
        assertDiagnosticInFile(sweep(), "SwElseUse.kt", "exhaustive")
    }

    // TC-LEAF-077: sealed class 基底では value / enum / interface 末端が言語上不可
    // （これらの末端種別は sealed interface 基底でのみ到達可能という境界の固定）
    @Test
    fun sealedClassBaseRejectsNonClassLeafShapes() {
        val output = sweep()
        assertDiagnosticInFile(output, "SwScValue.kt", "e: ")
        assertDiagnosticInFile(output, "SwScEnum.kt", "e: ")
        assertDiagnosticInFile(output, "SwScIface.kt", "e: ")
    }

    // TC-GAP-004(a): @Enumize 無しの sealed fun interface 自体を言語が拒否する
    // （(b) = @Enumize 付きの合流は TC-DIAG-007 が実証済み。V9 は言語不許容側で確定）
    @Test
    fun sealedFunInterfaceIsRejectedByLanguageItself() {
        assertDiagnosticInFile(sweep(), "SwSfi.kt", DiagFragments.LANG_SEALED_FUN_INTERFACE)
    }

    // TC-MPP-037: reified ヘルパ（enumishEntries / enumishValueOf / enumishValueOfOrNull）は
    // runtime-api に存在しない（V6 欠番）→ 未解決参照
    @Test
    fun reifiedHelpersDoNotExist() {
        val output = sweep()
        assertDiagnosticInFile(output, "SwReified.kt", "enumishEntries")
        assertDiagnosticInFile(output, "SwReified.kt", "enumishValueOf")
        assertDiagnosticInFile(output, "SwReified.kt", "enumishValueOfOrNull")
    }

    // TC-DIAG-106: @EnumizeLabel は v1 の runtime-api に存在せず未解決参照になる
    // （ENUMIZE_INVALID_LABEL の発火経路が無いことの固定）
    @Test
    fun enumizeLabelAliasDoesNotExistInV1() {
        assertDiagnosticInFile(sweep(), "SwElabel.kt", "EnumizeLabel")
    }
}
