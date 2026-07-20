package org.wrongwrong.diag.xnp

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-096 用の基底（末端 interface 2 つ）
@Enumize
sealed interface XnpSi {
    interface LeafA : XnpSi

    interface LeafB : XnpSi
}
