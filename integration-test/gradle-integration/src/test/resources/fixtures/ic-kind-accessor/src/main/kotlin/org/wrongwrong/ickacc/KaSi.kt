package org.wrongwrong.ickacc

import org.wrongwrong.sealedClassEnumizer.Enumize

// 参照不能 kind 用 IR-only アクセサの IC 決定性フィクスチャ（概要 §8・設計02 §4.3）。
// createEntries はトップレベルアクセサ（KaHidden.Leaf）とネストアクセサ（KaPriv の private companion）を
// 呼んで entries を組む。編集・再ビルド・キャッシュ復元で生成物のバイトと entries が決定的であることを観測する
@Enumize
sealed interface KaSi {
    // 基底内ネストの public 末端（参照可 = 直接参照）
    data object Visible : KaSi
}
