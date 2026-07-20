package org.wrongwrong.diag.exposed

// TC-DIAG-093: sealed class 階層で基底より広い末端 → 言語 EXPOSED_SUPER_CLASS（プラグイン診断へは到達しない）
class ExpLeaf : ExpBase()
