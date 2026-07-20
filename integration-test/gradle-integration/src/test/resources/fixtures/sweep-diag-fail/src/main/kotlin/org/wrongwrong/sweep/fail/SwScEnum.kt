package org.wrongwrong.sweep.fail

// TC-LEAF-077: enum class は class を継承できない → 言語エラー（sealed class 基底では enum 末端不可）
enum class SwScEnum : SwSc() {
    ONE,
}
