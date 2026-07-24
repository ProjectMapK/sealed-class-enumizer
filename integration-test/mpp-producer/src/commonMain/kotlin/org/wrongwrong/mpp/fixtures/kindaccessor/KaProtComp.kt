package org.wrongwrong.mpp.fixtures.kindaccessor

// protected な既存 companion（kind）→ 末端クラス内にネストした IR-only アクセサ経由で load
abstract class KaProtComp : KaRoot {
    protected companion object
}
