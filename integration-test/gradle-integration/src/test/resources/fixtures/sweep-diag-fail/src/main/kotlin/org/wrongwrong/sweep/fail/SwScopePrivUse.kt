package org.wrongwrong.sweep.fail

// TC-VIS-002: private トップレベル基底を別ファイルから参照 → 言語の可視性エラー
// （プラグイン独自診断は出さない = ENUMIZE_ 断片の非存在をテスト側で確認する）
fun swScopePrivUse(): Int = SwScopePriv.Enumish.entries.size
