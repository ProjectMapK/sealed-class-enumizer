package org.wrongwrong.fixtures.tostring

// toString を抽象として再宣言する親クラス（TC-BOX-047。生成は充足を肩代わりせず、kind 側の手動実装が必要）
abstract class Demanding {
    abstract override fun toString(): String
}
