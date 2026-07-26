package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-51 用: 間接一致の経由 interface（スキップ推移化の回帰）
interface OkIndBase : Enumized<OkIndSi.Enumish>
