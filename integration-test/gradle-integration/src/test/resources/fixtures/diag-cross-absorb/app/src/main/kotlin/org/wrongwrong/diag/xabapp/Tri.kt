package org.wrongwrong.diag.xabapp

import org.wrongwrong.diag.xab.XabSi

// TC-DIAG-023: 別モジュールでの単一末端サブタイプ → 非発火（新 kind を作らず Poly の kind に吸収）
class Tri : XabSi.Poly()
