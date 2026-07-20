package org.wrongwrong.sweep.fail

// TC-VIS-009: protected ネスト基底を非サブクラス位置から参照 → 言語の可視性エラー（プラグイン診断なし）
fun swProtHostUse(x: SwProtHost.P): String = x.toString()
