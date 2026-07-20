package org.wrongwrong.icfix

import org.wrongwrong.sealedClassEnumizer.Enumize

// IC 回帰フィクスチャの基底。末端は別ファイルへ分散する（docs/テストケース管理.md TC-IC-003 の L-03 配置）
@Enumize
sealed interface SI
