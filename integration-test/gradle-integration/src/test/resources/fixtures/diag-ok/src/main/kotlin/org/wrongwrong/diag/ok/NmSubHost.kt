package org.wrongwrong.diag.ok

// docs/test/ケース04-診断.md DIA-36: 階層外サブタイプの単純名（DupN）は判定非関与 → 非発火
class NmSubHost {
    class DupN : NmSubBase.PolyN()
}
