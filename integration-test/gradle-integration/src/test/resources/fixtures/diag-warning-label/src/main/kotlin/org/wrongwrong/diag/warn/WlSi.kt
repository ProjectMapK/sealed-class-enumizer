package org.wrongwrong.diag.warn

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-063/080: 末端 class 本体の label メンバー → ENUMIZE_EXTENSION_SHADOWED（警告のみ・MMC ではない）
@Enumize
sealed interface WlSi {
    data class Tagged(val label: String) : WlSi

    // TC-DIAG-065: label 以外の名前のメンバー → 警告なし
    data class Named(val name: String) : WlSi
}
