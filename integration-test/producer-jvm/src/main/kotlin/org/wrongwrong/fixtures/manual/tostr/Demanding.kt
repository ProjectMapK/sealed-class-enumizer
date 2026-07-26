package org.wrongwrong.fixtures.manual.tostr

// toString を抽象として再宣言する親クラス（docs/test/ケース01-生成と実行時API.md API-37。
// 生成は充足を肩代わりせず、kind 側の手動実装が必要）
abstract class Demanding {
    abstract override fun toString(): String
}
