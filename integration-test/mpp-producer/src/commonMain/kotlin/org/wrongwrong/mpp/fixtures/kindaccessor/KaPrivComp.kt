package org.wrongwrong.mpp.fixtures.kindaccessor

// private companion（kind）→ 末端クラス内にネストした IR-only アクセサ経由で load
class KaPrivComp(val v: Int) : KaRoot {
    private companion object
}
