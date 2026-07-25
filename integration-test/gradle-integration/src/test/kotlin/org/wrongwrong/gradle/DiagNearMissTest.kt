package org.wrongwrong.gradle

import kotlin.test.Test
import org.wrongwrong.gradle.DiagAsserts.assertFragmentAbsent

// G 軸: 単一モジュールの near-miss（非発火）集約フィクスチャ。互いに独立な階層を 1 ビルドにまとめて
// 「ビルド成功 + 診断断片の不在」で非発火を検証する（docs/テストケース管理.md TC-DIAG-017・037・041・
// 049・052・055・057〜058・060・070・083・095・097・099〜100・102・104・109〜113）
class DiagNearMissTest : DiagTestBase() {
    private fun nearMiss(): String = successOutput("diag-nearmiss-general", "compileKotlin")

    // TC-DIAG-017: 単一家族 + 非 @Enumize interface 併用（NmCross.kt）
    // TC-DIAG-095: 単一家族内のダイヤモンド（NmDia.kt）
    @Test
    fun familyNearMissesDoNotReport() {
        assertFragmentAbsent(nearMiss(), DiagFragments.MULTIPLE_FAMILIES)
        assertFragmentAbsent(nearMiss(), DiagFragments.NESTED_IN_HIERARCHY)
    }

    // TC-DIAG-037: 階層外クラスの companion が単独で末端（NmHost.kt）
    // TC-DIAG-097: inner class が非 final 末端のサブタイプ（NmInHost.kt）
    @Test
    fun kindNearMissesDoNotReport() {
        assertFragmentAbsent(nearMiss(), DiagFragments.COMPANION_LEAF_CONFLICT)
        assertFragmentAbsent(nearMiss(), DiagFragments.INNER_LEAF)
        assertFragmentAbsent(nearMiss(), DiagFragments.AMBIGUOUS_KIND)
    }

    // TC-DIAG-041: kind を担うだけの named companion の同名（NmFac.kt）
    // TC-DIAG-099: 末端の単純名が中間 sealed の名前と一致（NmMid.kt / NmMidOuter.kt）
    @Test
    fun labelNearMissesDoNotClash() {
        assertFragmentAbsent(nearMiss(), DiagFragments.LABEL_CLASH)
    }

    // supertype の表記に依らず末端として成立する（kind は明示 companion か自動生成で必ず揃うため、
    // 言語の「abstract member 未実装」へ落ちない = 階層判定の欠落が無いこと。docs/コンパイラプラグイン設計01.md §6.1・§6.2）
    // TC-DIAG-057: typealias 経由・companion 無し（NmAlNoc.kt）
    // TC-DIAG-058: import エイリアス経由・companion 無し（NmImNoc.kt）
    // TC-DIAG-060: typealias 経由の明示 companion（NmAlFoo.kt）
    // TC-DIAG-102: 末端 object は companion 概念が無い（NmAlBar.kt）
    // TC-DIAG-109: import エイリアス経由の明示 companion（NmImFoo.kt）
    // TC-DIAG-110: import エイリアス経由の末端 object（NmImBar.kt）
    // TC-DIAG-111: FQN 表記の supertype（NmFqFoo.kt）
    // TC-DIAG-112: 別パッケージ typealias の明示 import（NmFarNoc.kt）
    // TC-DIAG-113: 別パッケージ typealias の star import（NmStarNoc.kt）
    @Test
    fun everySupertypeNotationYieldsAKind() {
        assertFragmentAbsent(nearMiss(), DiagFragments.LANG_ABSTRACT_MEMBER_NOT_IMPLEMENTED)
    }

    // TC-DIAG-049: 型引数一致の生成 Enumish supertype の手動重複（NmDup.kt）
    // TC-DIAG-052: 手動 : Enumized<自身の Enumish>（NmSelf.kt）
    // TC-DIAG-100: 基底が runtime-api の基底 Enumish を手動継承（NmBase.kt）
    @Test
    fun matchingManualSupertypesDoNotMismatch() {
        assertFragmentAbsent(nearMiss(), DiagFragments.MANUAL_SUPERTYPE_MISMATCH)
        assertFragmentAbsent(nearMiss(), DiagFragments.MANUAL_IMPL_OUTSIDE_HIERARCHY)
    }

    // TC-DIAG-055: 非 @Enumize クラスのネスト Enumish（NmRnc.kt）
    @Test
    fun nestedEnumishOutsideEnumizeDoesNotClash() {
        assertFragmentAbsent(nearMiss(), DiagFragments.RESERVED_NAME_CLASH)
    }

    // TC-DIAG-070: 基底 Enumish（runtime-api）の直接実装は無制約（NmRt.kt）
    // TC-DIAG-083: kind が継承経路上の具象 toString を持つ → 生成スキップのみで診断なし（NmTsLeaf.kt）
    // TC-DIAG-104: 継承者ゼロの空階層（NmEmpty.kt）
    @Test
    fun remainingNearMissesDoNotConflict() {
        assertFragmentAbsent(nearMiss(), DiagFragments.MANUAL_MEMBER_CONFLICT)
        assertFragmentAbsent(nearMiss(), DiagFragments.NOT_SEALED)
        assertFragmentAbsent(nearMiss(), DiagFragments.ENUM_LEAF_UNSUPPORTED)
    }
}
