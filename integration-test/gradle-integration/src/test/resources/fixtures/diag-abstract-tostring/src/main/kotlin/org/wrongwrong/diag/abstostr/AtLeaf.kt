package org.wrongwrong.diag.abstostr

// TC-DIAG-084: kind の supertype が toString を抽象再宣言 → 言語エラー（プラグイン診断は非発火）
object AtLeaf : AtAbs(), AtSi
