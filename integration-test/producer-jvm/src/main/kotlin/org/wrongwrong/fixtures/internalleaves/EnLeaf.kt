package org.wrongwrong.fixtures.internalleaves

// internal enum class 末端（TC-GAP-015）。enum 全体で 1 kind（= companion）。
// label は enum class 宣言名・name は定数名で管轄分離（docs/概要.md §4）
internal enum class EnLeaf : InternalLeaves {
    A,
    B,
    ;

    internal companion object
}
