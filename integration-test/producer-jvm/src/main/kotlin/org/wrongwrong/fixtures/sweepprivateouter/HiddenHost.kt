package org.wrongwrong.fixtures.sweepprivateouter

// TC-LEAF-090: 別ファイルの private 外側クラスにネストした末端（外側チェーンにより基底本体
// スコープから名前参照できない配置。実測ではこの構成でも生成・実行が成立する）
private class HiddenHost {
    object Leaf : PrivateOuterRoot
}
