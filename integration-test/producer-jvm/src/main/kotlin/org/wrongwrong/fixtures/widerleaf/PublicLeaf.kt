package org.wrongwrong.fixtures.widerleaf

// 基底（internal）より広い可視性の末端。sealed interface 階層では言語上成立する（エッジ §1.1 #2）。
// 階層 API（entries / valueOf）は internal、値・kind API（asEnumish / label）は public という分離が生じる
class PublicLeaf : InternalBase {
    companion object
}
