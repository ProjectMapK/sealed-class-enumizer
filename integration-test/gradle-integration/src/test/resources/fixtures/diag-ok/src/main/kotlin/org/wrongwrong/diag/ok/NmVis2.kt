package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-26: private 基底同士（同一ファイル並置そのものが検証対象のため
// トップレベル 2 宣言を同居させる = 1 ファイル 1 クラス規約の適用外）
@Enumize
private sealed interface NmVis2

private data object NmVis2Leaf : NmVis2
