package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-27 用の internal sealed class 基底
@Enumize
internal sealed class ExpBase

// DIA-27: sealed class 階層の広い末端 → 言語 EXPOSED_SUPER_CLASS・KTD 不在
class ExpLeaf : ExpBase()
