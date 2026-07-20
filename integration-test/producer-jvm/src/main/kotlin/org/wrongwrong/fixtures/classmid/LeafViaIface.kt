package org.wrongwrong.fixtures.classmid

// 中間 sealed interface 経由の末端・companion 明示なし（TC-LEAF-023）
class LeafViaIface(val v: Int) : MidIface
