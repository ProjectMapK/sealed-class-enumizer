package io.github.projectmapk.sweep.xnuse

import io.github.projectmapk.sweep.xn.SwXnInt

// docs/test/ケース05-境界横断.md XMP-15: internal 一色の階層は別モジュールから基底・生成 API・末端とも
// 参照不能（言語の可視性エラー。プラグイン独自診断は出さない）
fun useInternal(): Int = SwXnInt.Enumish.entries.size
