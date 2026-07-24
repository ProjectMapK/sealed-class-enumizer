package org.wrongwrong.diag.nmvis

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-030: 同一ファイルの private トップレベル末端 → 直接参照で load（別ファイル配置ならトップレベルアクセサ経由）。
// このケースは private トップレベル基底と末端の同一ファイル並置そのものが検証対象のため 2 宣言を同居させる
@Enumize
private sealed interface NmVis2

private data object NmVis2Leaf : NmVis2
