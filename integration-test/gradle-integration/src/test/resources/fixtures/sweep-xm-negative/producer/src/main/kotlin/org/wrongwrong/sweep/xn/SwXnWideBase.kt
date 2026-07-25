package org.wrongwrong.sweep.xn

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-VIS-038 用: internal 基底（基底より広い末端 SwXnWideLeaf を持つ。docs/エッジケースへの対応方針.md §1.1 #2 で言語上成立）
@Enumize
internal sealed interface SwXnWideBase
