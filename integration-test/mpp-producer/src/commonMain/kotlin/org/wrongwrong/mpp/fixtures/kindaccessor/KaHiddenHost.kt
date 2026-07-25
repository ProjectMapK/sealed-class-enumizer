package org.wrongwrong.mpp.fixtures.kindaccessor

// private 外側クラスにネストした参照不能末端 → 末端ファイルのトップレベル IR-only アクセサ経由で load
private class KaHiddenHost {
    object Leaf : KaRoot
}
