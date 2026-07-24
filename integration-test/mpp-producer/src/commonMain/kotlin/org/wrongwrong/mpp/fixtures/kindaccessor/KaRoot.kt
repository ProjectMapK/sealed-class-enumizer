package org.wrongwrong.mpp.fixtures.kindaccessor

import org.wrongwrong.sealedClassEnumizer.Enumize

// 参照不能 kind（private companion・private 外側クラスの末端・private トップレベル末端）が IR-only
// アクセサ経由で entries に載ることを、全 klib ターゲット（jvm / js / native / wasmJs / wasmWasi）で
// 実証する（概要 §8・設計02 §4.3）。トップレベル IR-only アクセサ生成の klib 直列化・リンク成立が焦点
@Enumize
sealed interface KaRoot {
    // 基底内ネストの public 末端（参照可能 = 直接参照）
    data object Visible : KaRoot
}
