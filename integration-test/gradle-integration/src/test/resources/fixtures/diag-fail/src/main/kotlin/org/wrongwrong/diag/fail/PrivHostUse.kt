package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-28: private ネスト基底を外側クラスの外から参照 → 言語可視性エラーのみ
fun privHostUse(x: PrivHost.N): String = x.toString()
