package org.wrongwrong.icfix

import org.wrongwrong.sealedClassEnumizer.Enumize

// IC 回帰フィクスチャの基底。末端は別ファイルへ分散する（多ファイル分散配置）
@Enumize
sealed interface SI
