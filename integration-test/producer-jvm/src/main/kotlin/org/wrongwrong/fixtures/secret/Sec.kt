package org.wrongwrong.fixtures.secret

import org.wrongwrong.sealedClassEnumizer.Enumize

// 基底本体にネストした private / internal 末端（TC-LEAF-088 / TC-BOX-073 / TC-ORD-059 / TC-VIS-012 /
// TC-VIS-033）。
// 基底内ネストの private 末端は entries 構築コード（基底本体スコープ）から参照できるため
// アクセサ不要（直接参照）で entries に載り、可視性は entries の順序にも掲載にも影響しない
@Enumize
sealed interface Sec {
    private data object Zzz : Sec

    data object Aaa : Sec

    // TC-ORD-059 の配置は「internal object Mmm をネスト」とするが、interface 本体のメンバーに internal は
    // 言語上付けられない（e: Modifier 'internal' is not applicable inside 'interface'）ため private で代替する。
    // ケースの本質（可視性が順序・掲載に影響しない）は private / public の混在で保たれる
    private data object Mmm : Sec

    // private class 末端（kind は自動生成 companion。実効可視性 private でも規則 1 で成立）
    private class Cls(val v: Int) : Sec
}
