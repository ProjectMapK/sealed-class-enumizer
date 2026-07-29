package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-47 用の間接継承元
interface Ms2MyBase : Enumized<MsWrong>
