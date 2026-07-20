package org.wrongwrong.diag.nm

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-017: 単一 @Enumize 家族 + 非 @Enumize interface の併用 → MULTIPLE_FAMILIES 非発火
@Enumize
sealed interface NmCross {
    interface Marker

    data object X : NmCross, Marker
}
