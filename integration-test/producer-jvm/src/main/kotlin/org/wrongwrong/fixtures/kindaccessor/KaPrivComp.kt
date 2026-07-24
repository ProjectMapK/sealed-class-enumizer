package org.wrongwrong.fixtures.kindaccessor

// private な既存 companion（kind）。基底本体から名前参照できないため、末端クラス内にネストした
// IR-only アクセサ経由で entries に載る
class KaPrivComp(val v: Int) : KaRoot {
    private companion object
}
