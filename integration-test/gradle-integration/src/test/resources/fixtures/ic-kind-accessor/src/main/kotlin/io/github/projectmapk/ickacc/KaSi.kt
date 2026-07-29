package io.github.projectmapk.ickacc

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 参照不能 kind 用 IR-only アクセサの IC 決定性フィクスチャ（docs/概要.md §8・docs/コンパイラプラグイン設計02.md §4.3）。
// createEntries はトップレベルアクセサ（KaHidden.Leaf）とネストアクセサ（KaPriv の private companion）を
// 呼んで entries を組む。編集・再ビルド・キャッシュ復元で生成物のバイトと entries が決定的であることを観測する
@Enumize
sealed interface KaSi {
    // 基底内ネストの public 末端（参照可 = 直接参照）
    data object Visible : KaSi
}
