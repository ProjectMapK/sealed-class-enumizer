package org.wrongwrong.probe.deep

// docs/test/ケース04-診断.md DIA-68: 多段 private 壁（private トップレベルクラス内の
// ネスト kind）でのアクセサ生成の帰結を固定する（単一壁のみ対応の設計制約の境界）
private class DpOuter {
    object Leaf : DpSi
}
