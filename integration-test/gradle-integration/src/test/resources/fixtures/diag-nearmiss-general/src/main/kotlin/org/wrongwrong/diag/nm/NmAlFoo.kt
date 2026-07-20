package org.wrongwrong.diag.nm

// TC-DIAG-060: typealias 経由でも明示 companion があれば非発火（明示は常に完全なワークアラウンド）
class NmAlFoo(val v: Int) : NmAlT {
    companion object
}
