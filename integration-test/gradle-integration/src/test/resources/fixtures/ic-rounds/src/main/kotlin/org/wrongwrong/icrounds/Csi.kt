package org.wrongwrong.icrounds

import org.wrongwrong.sealedClassEnumizer.Enumize

// 多段中間チェーンの基底（CSI ← Mid1 ← Mid2 ← ChLeaf を各別ファイルに配置する）
@Enumize
sealed interface CSI
