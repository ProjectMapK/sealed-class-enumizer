package org.wrongwrong.sweep.fail

// TC-VIS-007: private ネスト基底を外側クラスの外から参照 → 言語の可視性エラー（プラグイン診断なし）
fun swPrivHostUse(x: SwPrivHost.N): String = x.toString()
