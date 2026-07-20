package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-LEAF-077 用の sealed class 基底（value / enum / interface 末端の言語境界の確認）
@Enumize
sealed class SwSc {
    // 正常な末端（境界ケースの巻き添えで基底自体が空にならないための対照）
    data object Ok : SwSc()
}
