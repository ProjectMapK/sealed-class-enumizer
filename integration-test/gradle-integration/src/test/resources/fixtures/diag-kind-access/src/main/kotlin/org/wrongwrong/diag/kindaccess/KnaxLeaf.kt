package org.wrongwrong.diag.kindaccess

// TC-DIAG-088: 基底より広い末端 + private companion。companion はアクセサで load するが eff(T=internal 基底)<eff(L=public 末端) で asEnumish 返り値型が規則 3 の KIND_TYPE_NOT_DENOTABLE のみ発火
class KnaxLeaf : KnaxSi {
    private companion object
}
