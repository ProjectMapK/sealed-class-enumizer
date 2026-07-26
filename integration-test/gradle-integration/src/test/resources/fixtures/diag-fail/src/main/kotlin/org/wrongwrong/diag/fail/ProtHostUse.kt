package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-28: protected ネスト基底を非サブクラス位置から参照 → 言語可視性エラーのみ
fun protHostUse(x: ProtHost.P): String = x.toString()
