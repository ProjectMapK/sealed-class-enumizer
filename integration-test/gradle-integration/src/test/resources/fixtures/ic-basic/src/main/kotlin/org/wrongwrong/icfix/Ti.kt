package org.wrongwrong.icfix

import org.wrongwrong.sealedClassEnumizer.Enumize

// 第 1 階層と独立した第 2 階層（P3 非集約 = docs/テストケース管理.md TC-IC-041/042 の非 dirty 観測用）
@Enumize
sealed interface TI {
    data object T1 : TI
}
