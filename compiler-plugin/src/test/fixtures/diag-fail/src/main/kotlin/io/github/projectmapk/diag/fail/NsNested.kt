package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-02: 非 sealed 末端への付与 → NOT_SEALED と NESTED_IN_HIERARCHY の併発
@Enumize
sealed interface NsNested {
    @Enumize
    class Leaf : NsNested
}
