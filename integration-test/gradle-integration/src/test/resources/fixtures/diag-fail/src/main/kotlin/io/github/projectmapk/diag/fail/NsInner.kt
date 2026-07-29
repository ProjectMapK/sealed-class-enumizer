package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-01: inner class への付与（inner は sealed 不能の帰結）→ ENUMIZE_NOT_SEALED
class NsInnerHost {
    @Enumize
    inner class NsInner
}
