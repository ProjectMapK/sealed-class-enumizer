package io.github.projectmapk.fixtures.vis.narrow

// 基底（internal）より広い public 末端 × 明示 public companion（docs/test/ケース02-可視性.md VIS-19 =
// E-2 生成側。sealed interface 階層では言語上成立し（診断なし=コンパイル成功が兼ねる）、
// 階層 API（entries / valueOf）は internal・値/kind API（asEnumish / label）は public という分離が生じる）
class PublicLeaf(val v: Int) : NarrowBase {
    companion object
}
