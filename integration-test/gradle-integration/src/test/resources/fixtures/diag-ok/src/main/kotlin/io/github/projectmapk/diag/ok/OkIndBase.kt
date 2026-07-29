package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-51 用: 間接一致の経由 interface（スキップ推移化の回帰）
interface OkIndBase : Enumized<OkIndSi.Enumish>
