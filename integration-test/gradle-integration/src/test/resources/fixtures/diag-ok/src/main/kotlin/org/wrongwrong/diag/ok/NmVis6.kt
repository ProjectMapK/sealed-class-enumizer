package org.wrongwrong.diag.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-26: internal 基底 + 広い末端 + public companion（規則 1 成立）
// → KTD 非発火。基底と末端の同居がテストの本質のため 1 ファイル 1 クラス規約は適用外
@Enumize
internal sealed interface NmVis6

class NmVis6Wide : NmVis6 {
    companion object
}
