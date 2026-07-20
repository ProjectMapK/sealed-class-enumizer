package org.wrongwrong.sweep.fail

// TC-LEAF-077: interface は class を継承できない → 言語エラー（sealed class 基底では interface 末端不可）
interface SwScIface : SwSc
