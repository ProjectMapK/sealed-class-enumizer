package org.wrongwrong.diag.xamb

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-021 用の基底（末端 interface 2 つ）
@Enumize
sealed interface XambSi {
    interface LeafA : XambSi

    interface LeafB : XambSi
}
