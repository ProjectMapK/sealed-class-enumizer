package org.wrongwrong.sweep.xnuse

import org.wrongwrong.sweep.xn.SwXnInt

// TC-VIS-004 / TC-XM-051: internal 一色の階層は別モジュールから基底・生成 API・末端とも
// 参照不能（言語の可視性エラー。プラグイン独自診断は出さない）
fun useInternal(): Int = SwXnInt.Enumish.entries.size
