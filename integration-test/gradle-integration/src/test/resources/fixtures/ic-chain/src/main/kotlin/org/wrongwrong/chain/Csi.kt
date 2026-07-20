package org.wrongwrong.chain

import org.wrongwrong.sealedClassEnumizer.Enumize

// 3 段チェーンの基底（CSI ← Mid1 ← Mid2 ← Leaf を各別ファイルに配置する）
@Enumize
sealed interface CSI
