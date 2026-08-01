package io.github.projectmapk.mpp.fixtures.kindaccessor

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 参照不能 kind（private companion・private 外側クラスの末端・private トップレベル末端）が IR-only
// アクセサ経由で entries に載ることを、全ターゲット（jvm / js / native / wasmJs / wasmWasi）で
// 実証する（docs/概要.md §8・docs/コンパイラプラグイン設計02.md §4.3）。
// 本ファイルは末端クラス内へネストするアクセサ形を担い、トップレベルアクセサ形（= 基底のファイル外
// 配置が壁の成立条件となる末端）は KaWalledLeaves.kt が担う。
// トップレベル IR-only アクセサ生成の klib 直列化・リンク成立が焦点
@Enumize
sealed interface KaRoot {
    // 基底内ネストの public 末端（参照可能 = 直接参照）
    data object Visible : KaRoot
}

// private companion（kind）→ 末端クラス内にネストした IR-only アクセサ経由で load
class KaPrivComp(val v: Int) : KaRoot {
    private companion object
}

// protected な既存 companion（kind）→ 末端クラス内にネストした IR-only アクセサ経由で load
abstract class KaProtComp : KaRoot {
    protected companion object
}

// interface 末端の private companion（kind）→ 末端 interface 内にネストした IR-only アクセサ経由で load
// （interface メンバーは public / private のみ。nested object から private companion を intra-scope 参照する）
interface KaIface : KaRoot {
    private companion object
}
