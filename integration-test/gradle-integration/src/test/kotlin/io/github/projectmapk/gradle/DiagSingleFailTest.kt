package io.github.projectmapk.gradle

import io.github.projectmapk.gradle.DiagAsserts.assertDiagnosticAt
import io.github.projectmapk.gradle.DiagAsserts.assertDiagnosticInFile
import io.github.projectmapk.gradle.DiagAsserts.assertFragmentAbsentAt
import kotlin.test.Test

// 単一モジュールの発火系・言語委譲を diag-fail フィクスチャの 1 buildAndFail で全件検証する
// （docs/test/ケース04-診断.md の DIA 発火ケース群・docs/test/フィクスチャ構成.md §3）。
// プラグイン診断は file:line 照合、言語診断は報告行が言語管轄のためファイル内断片照合で観測する。
// DIA-71 のみ diag-test-source フィクスチャの追加ビルド（compileTestKotlin）を使う
class DiagSingleFailTest : DiagTestBase() {
    private fun fail(): String = failOutput("diag-fail", "compileKotlin")

    // docs/test/ケース04-診断.md DIA-01: K1 負値の全種別で NOT_SEALED が各宣言位置に発火する
    @Test
    fun notSealedFiresForEveryClassifierKind() {
        val output = fail()
        // 非 object 宣言はアノテーション行・object 宣言は object キーワード行に報告される
        listOf(
                "NsEnum.kt" to 6,
                "NsAnnotation.kt" to 6,
                "NsObject.kt" to 7,
                "NsDataObject.kt" to 7,
                "NsOpenClass.kt" to 6,
                "NsAbstractClass.kt" to 6,
                "NsFinalClass.kt" to 6,
                "NsDataClass.kt" to 6,
                "NsValueClass.kt" to 6,
                "NsInterface.kt" to 6,
                "NsFunInterface.kt" to 6,
                "NsInner.kt" to 7,
            )
            .forEach { (file, line) ->
                assertDiagnosticAt(output, file, line, DiagFragments.NOT_SEALED)
            }
    }

    // docs/test/ケース04-診断.md DIA-02: 非 sealed 末端への付与は NS と NESTED が併発する
    @Test
    fun annotatedNonSealedLeafReportsBoth() {
        val output = fail()
        assertDiagnosticAt(output, "NsNested.kt", 8, DiagFragments.NOT_SEALED)
        assertDiagnosticAt(output, "NsNested.kt", 8, DiagFragments.NESTED_IN_HIERARCHY)
    }

    // docs/test/ケース04-診断.md DIA-03: sealed fun interface は言語 unsupported へ合流・NS 不在
    @Test
    fun sealedFunInterfaceMergesIntoLanguageError() {
        val output = fail()
        assertDiagnosticAt(output, "SealedFun.kt", 7, DiagFragments.LANG_SEALED_FUN_INTERFACE)
        assertFragmentAbsentAt(output, "SealedFun.kt", DiagFragments.NOT_SEALED)
    }

    // docs/test/ケース04-診断.md DIA-04: 非クラス対象への付与は言語 WRONG_ANNOTATION_TARGET のみ
    @Test
    fun wrongTargetsFailWithLanguageErrorOnly() {
        val output = fail()
        listOf(6, 9, 12).forEach { line ->
            assertDiagnosticAt(
                output,
                "NsTargets.kt",
                line,
                DiagFragments.LANG_WRONG_ANNOTATION_TARGET,
            )
        }
        assertFragmentAbsentAt(output, "NsTargets.kt", DiagFragments.NOT_SEALED)
    }

    // docs/test/ケース04-診断.md DIA-13: 兄弟 2 基底の交差で MH + 基底 FQN 2 件
    @Test
    fun siblingHierarchyCrossingIsReported() {
        assertDiagnosticAt(
            fail(),
            "MultCross.kt",
            4,
            DiagFragments.MULTIPLE_HIERARCHIES,
            "MultA",
            "MultB",
        )
    }

    // docs/test/ケース04-診断.md DIA-14: 中間 sealed の交差は中間と末端の双方の宣言へ報告される
    @Test
    fun intermediateHierarchyCrossingReportsBothParticipants() {
        val output = fail()
        assertDiagnosticAt(output, "MultMid.kt", 4, DiagFragments.MULTIPLE_HIERARCHIES)
        assertDiagnosticAt(output, "MultMidLeaf.kt", 4, DiagFragments.MULTIPLE_HIERARCHIES)
    }

    // docs/test/ケース04-診断.md DIA-15: 階層メンバー（末端・中間）自身への @Enumize は NESTED。
    // 自己の @Enumize が生成する supertype と外側基底の生成 supertype の食い違いで MSM が併発する
    @Test
    fun enumizeOnHierarchyMemberIsNested() {
        val output = fail()
        assertDiagnosticAt(
            output,
            "MultNested.kt",
            9,
            DiagFragments.NESTED_IN_HIERARCHY,
            "MultNested",
        )
        assertDiagnosticAt(output, "MultNestedMid.kt", 9, DiagFragments.NESTED_IN_HIERARCHY)
        assertDiagnosticAt(output, "MultNested.kt", 9, DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
    }

    // docs/test/ケース04-診断.md DIA-15: @Enumize 付き中間の配下末端には MH（上向き探索は 2 基底へ到達）
    @Test
    fun leafBelowAnnotatedIntermediateReportsMultipleHierarchies() {
        assertDiagnosticAt(fail(), "MultNestedMid.kt", 11, DiagFragments.MULTIPLE_HIERARCHIES)
    }

    // docs/test/ケース04-診断.md DIA-17: 末端サブタイプ + 基底直接実装形・2 末端 interface 実装形の AK
    @Test
    fun doubleLeafMembershipIsAmbiguous() {
        val output = fail()
        assertDiagnosticAt(output, "AmbB.kt", 4, DiagFragments.AMBIGUOUS_KIND, "AmbA")
        assertDiagnosticAt(output, "Amb2C.kt", 4, DiagFragments.AMBIGUOUS_KIND, "LeafA", "LeafB")
    }

    // docs/test/ケース04-診断.md DIA-23: inner 末端は IL 単独（手動メンバー併置でも後続検査スキップ）
    @Test
    fun innerLeafIsReportedAlone() {
        val output = fail()
        assertDiagnosticAt(output, "InnerHost.kt", 6, DiagFragments.INNER_LEAF)
        assertFragmentAbsentAt(output, "InnerHost.kt", DiagFragments.MEMBER_CONFLICT)
        assertFragmentAbsentAt(output, "InnerHost.kt", DiagFragments.KIND_TYPE_NOT_DENOTABLE)
    }

    // docs/test/ケース04-診断.md DIA-25: 規則 3（広い末端 + internal / private companion）で KTD
    @Test
    fun widerLeafWithNarrowCompanionIsNotDenotable() {
        val output = fail()
        assertDiagnosticAt(
            output,
            "KtdLeaf.kt",
            4,
            DiagFragments.KIND_TYPE_NOT_DENOTABLE,
            "KtdLeaf",
        )
        assertDiagnosticAt(output, "KnaxLeaf.kt", 4, DiagFragments.KIND_TYPE_NOT_DENOTABLE)
    }

    // docs/test/ケース04-診断.md DIA-27: sealed class 階層の広い末端は言語 EXPOSED_SUPER_CLASS・KTD 不在
    @Test
    fun sealedClassWiderLeafFailsWithExposureErrorOnly() {
        val output = fail()
        assertDiagnosticAt(output, "ExpLeaf.kt", 4, DiagFragments.LANG_EXPOSED_SUPER_CLASS)
        assertFragmentAbsentAt(output, "ExpLeaf.kt", DiagFragments.KIND_TYPE_NOT_DENOTABLE)
    }

    // docs/test/ケース04-診断.md DIA-28: 可視性スコープ違反と else 必須の位置依存 2 変種は言語エラーのみ
    @Test
    fun visibilityScopeViolationsFailWithoutPluginDiagnostics() {
        val output = fail()
        listOf("ScopePrivUse.kt", "PrivHostUse.kt", "ProtHostUse.kt").forEach { file ->
            assertDiagnosticInFile(output, file, "e: ")
        }
        assertDiagnosticInFile(output, "ElseIntUse.kt", DiagFragments.LANG_WHEN_NOT_EXHAUSTIVE)
        assertDiagnosticInFile(output, "ElsePubUse.kt", DiagFragments.LANG_WHEN_NOT_EXHAUSTIVE)
    }

    // docs/test/ケース04-診断.md DIA-63: 生成物 internal 化不成立と規則 2 フォールバックの言語根拠
    @Test
    fun languageFactsBehindVisibilityRules() {
        val output = fail()
        assertDiagnosticInFile(output, "WeakImpl.kt", DiagFragments.LANG_CANNOT_WEAKEN_ACCESS)
        assertDiagnosticInFile(output, "ExposeReturn.kt", DiagFragments.LANG_EXPOSED_RETURN_TYPE)
    }

    // docs/test/ケース04-診断.md DIA-29: companion 末端兼務は階層不問で CLC（報告位置 = companion）
    @Test
    fun companionAsLeafConflictsRegardlessOfHierarchy() {
        val output = fail()
        assertDiagnosticAt(output, "ClcHost.kt", 5, DiagFragments.COMPANION_LEAF_CONFLICT)
        assertDiagnosticAt(output, "Clc2Host.kt", 5, DiagFragments.COMPANION_LEAF_CONFLICT)
        assertDiagnosticAt(output, "Clc3Host.kt", 5, DiagFragments.COMPANION_LEAF_CONFLICT)
    }

    // docs/test/ケース04-診断.md DIA-33: 手動 companion の生成対象メンバー省略は生成が充足しエラー無し
    @Test
    fun omittedManualMembersAreFilledByGeneration() {
        assertFragmentAbsentAt(fail(), "OmitHost.kt", "e: ")
    }

    // docs/test/ケース04-診断.md DIA-34: 同一単純名の末端（enum 含む）は両当事者へ LC + 相手 FQN・
    // 相互非抑止・参照不能末端は無診断のまま同居する
    @Test
    fun sameLabelLeavesClashOnBothParticipants() {
        val output = fail()
        assertDiagnosticAt(output, "LcOuter1.kt", 5, DiagFragments.LABEL_CLASH, "LcOuter2")
        assertDiagnosticAt(output, "LcOuter2.kt", 5, DiagFragments.LABEL_CLASH, "LcOuter1")
        assertDiagnosticAt(output, "Lc3Si.kt", 8, DiagFragments.LABEL_CLASH, "Lc3Outer")
        assertDiagnosticAt(output, "Lc3Outer.kt", 5, DiagFragments.LABEL_CLASH)
        assertDiagnosticAt(output, "Lc4A.kt", 5, DiagFragments.LABEL_CLASH, "Lc4B")
        assertDiagnosticAt(output, "Lc4B.kt", 5, DiagFragments.LABEL_CLASH, "Lc4A")
        assertFragmentAbsentAt(output, "Lc4Priv.kt", "e: ")
    }

    // docs/test/ケース04-診断.md DIA-34: 3 末端同名では相手 FQN が列挙される引数形
    @Test
    fun tripleLabelClashListsAllPeers() {
        val output = fail()
        assertDiagnosticAt(output, "Lc5A.kt", 5, DiagFragments.LABEL_CLASH, "Lc5B", "Lc5C")
        assertDiagnosticAt(output, "Lc5B.kt", 5, DiagFragments.LABEL_CLASH, "Lc5A", "Lc5C")
        assertDiagnosticAt(output, "Lc5C.kt", 5, DiagFragments.LABEL_CLASH, "Lc5A", "Lc5B")
    }

    // docs/test/ケース04-診断.md DIA-35: companion 末端の宣言名 = label が他末端の単純名と衝突する
    @Test
    fun companionLeafDeclarationNameClashes() {
        val output = fail()
        assertDiagnosticAt(output, "Lc2Host.kt", 5, DiagFragments.LABEL_CLASH, "Foo2")
        assertDiagnosticAt(output, "Lc2Outer.kt", 5, DiagFragments.LABEL_CLASH)
    }

    // docs/test/ケース04-診断.md DIA-40: 末端 object の label / enumizedClass / asEnumish 手動宣言で
    // 各 MC + メンバー名（末端 object は kind 自身のため ES 警告は不発）
    @Test
    fun manualMembersOnLeafObjectConflict() {
        val output = fail()
        assertDiagnosticAt(output, "Mc1Si.kt", 11, DiagFragments.MEMBER_CONFLICT, "label")
        assertDiagnosticAt(output, "Mc1Si.kt", 13, DiagFragments.MEMBER_CONFLICT, "enumizedClass")
        assertDiagnosticAt(output, "Mc1Si.kt", 15, DiagFragments.MEMBER_CONFLICT, "asEnumish")
        assertFragmentAbsentAt(output, "Mc1Si.kt", DiagFragments.EXTENSION_SHADOWED)
    }

    // docs/test/ケース04-診断.md DIA-40: 判定は callable 名単位（関数形 label・ctor プロパティ
    // val asEnumish・引数付き過負荷の宣言種別交差でも MC）
    @Test
    fun memberKindCrossDeclarationsAlsoConflict() {
        val output = fail()
        assertDiagnosticAt(output, "Mc2Si.kt", 9, DiagFragments.MEMBER_CONFLICT, "label")
        assertDiagnosticAt(output, "Mc2Si.kt", 12, DiagFragments.MEMBER_CONFLICT, "asEnumish")
        assertDiagnosticAt(output, "Mc2Si.kt", 17, DiagFragments.MEMBER_CONFLICT, "asEnumish")
    }

    // docs/test/ケース04-診断.md DIA-41: 手動 kind companion（既定名・enum companion）の手動宣言で MC
    @Test
    fun manualMembersOnKindCompanionConflict() {
        val output = fail()
        assertDiagnosticAt(output, "Mc3Si.kt", 11, DiagFragments.MEMBER_CONFLICT, "label")
        assertDiagnosticAt(output, "Mc3Si.kt", 13, DiagFragments.MEMBER_CONFLICT, "enumizedClass")
        assertDiagnosticAt(output, "Mc4Enum.kt", 13, DiagFragments.MEMBER_CONFLICT, "label")
    }

    // docs/test/ケース04-診断.md DIA-42: 末端 class / interface の asEnumish 手動宣言で MC
    @Test
    fun manualAsEnumishOnLeafClassConflicts() {
        val output = fail()
        assertDiagnosticAt(output, "Mc5Leaf.kt", 7, DiagFragments.MEMBER_CONFLICT, "asEnumish")
        assertDiagnosticAt(output, "Mc6Si.kt", 9, DiagFragments.MEMBER_CONFLICT, "asEnumish")
    }

    // docs/test/ケース04-診断.md DIA-43: 階層外 interface からの同名具象 default 継承で MC
    // （label・asEnumish・enumizedClass 継承具象の変種）
    @Test
    fun inheritedDefaultMemberConflicts() {
        val output = fail()
        assertDiagnosticAt(output, "Mc7Leaf.kt", 4, DiagFragments.MEMBER_CONFLICT, "label")
        assertDiagnosticAt(output, "Mc8Leaf.kt", 4, DiagFragments.MEMBER_CONFLICT, "asEnumish")
        assertDiagnosticAt(output, "Mc9Leaf.kt", 4, DiagFragments.MEMBER_CONFLICT, "enumizedClass")
    }

    // docs/test/ケース04-診断.md DIA-70: クラス supertype の final 具象継承で MC — 階層外の直接継承・
    // 中間クラス介在・引数なし関数 asEnumish・階層内 sealed class 基底の ctor プロパティ・
    // 手動 kind companion（報告位置 = companion 宣言）
    @Test
    fun inheritedFinalClassMembersConflict() {
        val output = fail()
        assertDiagnosticAt(output, "FiLeaf.kt", 4, DiagFragments.MEMBER_CONFLICT, "label")
        assertDiagnosticAt(output, "FiViaMid.kt", 4, DiagFragments.MEMBER_CONFLICT, "label")
        assertDiagnosticAt(output, "FiAsLeaf.kt", 4, DiagFragments.MEMBER_CONFLICT, "asEnumish")
        assertDiagnosticAt(output, "FiScLeaf.kt", 4, DiagFragments.MEMBER_CONFLICT, "label")
        assertDiagnosticAt(output, "FiCls.kt", 5, DiagFragments.MEMBER_CONFLICT, "label")
    }

    // docs/test/ケース04-診断.md DIA-62: toString 抽象再宣言は言語 abstract 未実装のみ・MC 不在
    @Test
    fun abstractToStringRedeclarationFailsInLanguage() {
        val output = fail()
        assertDiagnosticInFile(
            output,
            "AtLeaf.kt",
            DiagFragments.LANG_ABSTRACT_MEMBER_NOT_IMPLEMENTED,
        )
        assertFragmentAbsentAt(output, "AtLeaf.kt", DiagFragments.MEMBER_CONFLICT)
    }

    // docs/test/ケース04-診断.md DIA-46: 基底の Enumized<別型> 直接継承で MSM（報告位置 = supertype ref）
    @Test
    fun directEnumizedMismatchIsReported() {
        assertDiagnosticAt(
            fail(),
            "Ms1Si.kt",
            8,
            DiagFragments.MANUAL_SUPERTYPE_MISMATCH,
            "MsWrong",
        )
    }

    // docs/test/ケース04-診断.md DIA-47: 自作基底 interface 経由（supertypeClosure）の不一致でも MSM
    @Test
    fun indirectEnumizedMismatchIsReported() {
        assertDiagnosticAt(fail(), "Ms2Si.kt", 6, DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
    }

    // docs/test/ケース04-診断.md DIA-48: 階層メンバー（末端）の Enumized<別型> でも MSM
    @Test
    fun leafSideEnumizedMismatchIsReported() {
        assertDiagnosticAt(fail(), "MsLeaf.kt", 6, DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
    }

    // docs/test/ケース04-診断.md DIA-49: E-1 適格 K も非適格 K 6 亜種も v1 は一律 MSM
    // （非 Enumish サブタイプ形のみ言語の境界違反が併発するため発火の事実だけを固定する）
    @Test
    fun customKindMismatchesInV1() {
        val output = fail()
        listOf(
                "Ms3Si.kt" to 8,
                "Ms4Si.kt" to 8,
                "Ms5Si.kt" to 8,
                "Ms6Si.kt" to 9,
                "Ms7Si.kt" to 9,
                "Ms8Si.kt" to 8,
            )
            .forEach { (file, line) ->
                assertDiagnosticAt(output, file, line, DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
            }
        assertDiagnosticInFile(output, "Ms9Si.kt", "e: ")
    }

    // docs/test/ケース04-診断.md DIA-50: 他階層 Enumish の Enumized 直接実装は MSM であって MH でない
    @Test
    fun crossHierarchyEnumizedIsMismatchNotMultipleHierarchies() {
        val output = fail()
        assertDiagnosticAt(output, "TfCross.kt", 6, DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
        assertFragmentAbsentAt(output, "TfCross.kt", DiagFragments.MULTIPLE_HIERARCHIES)
    }

    // docs/test/ケース04-診断.md DIA-53: projection / nullable 型引数は言語が先回りし MSM 不到達
    @Test
    fun projectionAndNullableArgumentsFailInLanguage() {
        val output = fail()
        assertDiagnosticInFile(output, "Proj.kt", DiagFragments.LANG_PROJECTION_IN_ARGUMENT)
        assertDiagnosticInFile(output, "NulArg.kt", DiagFragments.LANG_UPPER_BOUND_VIOLATED)
        assertFragmentAbsentAt(output, "Proj.kt", DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
        assertFragmentAbsentAt(output, "NulArg.kt", DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
    }

    // docs/test/ケース04-診断.md DIA-54: 既存ネスト Enumish（class / object / interface / 末端兼用）で RNC
    @Test
    fun existingNestedEnumishClashes() {
        val output = fail()
        listOf("Rn1.kt", "Rn2.kt", "Rn3.kt", "Rn4.kt").forEach { file ->
            assertDiagnosticAt(output, file, 8, DiagFragments.RESERVED_NAME_CLASH)
        }
    }

    // docs/test/ケース04-診断.md DIA-56: 階層外の生成 Enumish 直接実装（object・class + Enumized 併記・
    // 中間 companion 形・同一 module 別 pkg 形 = 言語 sealed 制約併発）で MIOH
    @Test
    fun outsideManualImplementationIsReported() {
        val output = fail()
        assertDiagnosticAt(
            output,
            "MiRogue.kt",
            7,
            DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY,
            "MiSi",
        )
        assertDiagnosticAt(output, "MiRogueCls.kt", 7, DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY)
        assertDiagnosticAt(output, "MiMid.kt", 8, DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY)
        assertDiagnosticAt(output, "AlienPkg.kt", 7, DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY)
        assertDiagnosticInFile(output, "AlienPkg.kt", "e: ", "package")
    }

    // docs/test/ケース04-診断.md DIA-60: MC / MSM 発火時も形状生成は継続し隣接参照は未解決にならない
    @Test
    fun conflictingDeclarationsKeepShapeGeneration() {
        assertFragmentAbsentAt(fail(), "ShapeProbe.kt", "e: ")
    }

    // docs/test/ケース04-診断.md DIA-61: @EnumizeLabel は v1 に存在せず未解決参照になる
    @Test
    fun enumizeLabelDoesNotExistInV1() {
        assertDiagnosticInFile(fail(), "Elabel.kt", "EnumizeLabel")
    }

    // docs/test/ケース04-診断.md DIA-61: reified ヘルパは v1 に存在せず未解決参照になる
    @Test
    fun reifiedHelpersDoNotExist() {
        val output = fail()
        assertDiagnosticInFile(output, "Reified.kt", "enumishEntries")
        assertDiagnosticInFile(output, "Reified.kt", "enumishValueOf")
        assertDiagnosticInFile(output, "Reified.kt", "enumishValueOfOrNull")
    }

    // docs/test/ケース04-診断.md DIA-64: sealed class 基底の非 class 末端種別は言語エラーへ委譲
    @Test
    fun sealedClassBaseRejectsNonClassLeafShapes() {
        val output = fail()
        listOf("ScValue.kt", "ScEnum.kt", "ScIface.kt", "ScAnn.kt").forEach { file ->
            assertDiagnosticInFile(output, file, "e: ")
        }
    }

    // docs/test/ケース04-診断.md DIA-65: 別パッケージ末端は sealed の同一パッケージ制約（言語）へ委譲
    @Test
    fun differentPackageLeafFailsWithSealedRule() {
        val output = fail()
        assertDiagnosticInFile(output, "FarLeaf.kt", "e: ", "package")
        assertFragmentAbsentAt(output, "FarLeaf.kt", DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY)
    }

    // docs/test/ケース04-診断.md DIA-71: main 基底 × test 末端は sealed の同一モジュール
    // （コンパイル単位）制約の言語エラーへ委譲・プラグイン診断は不在
    @Test
    fun testCompilationLeafFailsWithSealedRule() {
        val output = failOutput("diag-test-source", "compileTestKotlin")
        assertDiagnosticInFile(output, "TsLeaf.kt", "e: ")
        assertFragmentAbsentAt(output, "TsLeaf.kt", DiagFragments.NESTED_IN_HIERARCHY)
        assertFragmentAbsentAt(output, "TsLeaf.kt", DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY)
    }
}
