package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-25: 基底より広い末端 + private companion（アクセサ解決後も規則 3 のみ残る）
class KnaxLeaf : KnaxSi {
    private companion object
}
