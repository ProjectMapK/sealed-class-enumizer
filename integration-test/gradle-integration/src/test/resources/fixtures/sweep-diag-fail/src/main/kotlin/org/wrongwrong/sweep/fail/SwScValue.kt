package org.wrongwrong.sweep.fail

// TC-LEAF-077: value class は class を継承できない → 言語エラー（sealed class 基底では value 末端不可）
@JvmInline
value class SwScValue(val v: Int) : SwSc()
