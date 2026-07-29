package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-16: 非 @Enumize interface の併用 → MULTIPLE_HIERARCHIES 非発火
@Enumize
sealed interface NmCross {
    interface Marker

    data object X : NmCross, Marker
}
