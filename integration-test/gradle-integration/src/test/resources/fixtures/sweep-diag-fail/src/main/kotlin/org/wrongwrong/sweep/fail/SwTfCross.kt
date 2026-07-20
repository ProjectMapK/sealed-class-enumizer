package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumized

// TC-MAN-071: 末端が他階層の生成 Enumish を型引数とする Enumized を手動継承
// → 自階層の Enumized<SwTfA.Enumish> と型引数が衝突し ENUMIZE_MANUAL_SUPERTYPE_MISMATCH
// （基底 SwTfA・SwTfB 双方への帰属ではないため ENUMIZE_MULTIPLE_FAMILIES ではない切り分け境界）
data object SwTfCross : SwTfA, Enumized<SwTfB.Enumish>
