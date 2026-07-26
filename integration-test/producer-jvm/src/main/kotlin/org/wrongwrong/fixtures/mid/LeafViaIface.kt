package org.wrongwrong.fixtures.mid

// 中間 sealed interface 経由の末端・companion 明示なし（docs/test/ケース01-生成と実行時API.md API-29）
class LeafViaIface(val v: Int) : MidIface
