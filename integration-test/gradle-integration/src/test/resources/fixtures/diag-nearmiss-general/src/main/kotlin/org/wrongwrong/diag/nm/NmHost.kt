package org.wrongwrong.diag.nm

// TC-DIAG-037: 階層外クラスの companion が単独で末端 → 非発火（kind=companion・label=宣言名）
class NmHost {
    companion object : NmHostBase
}
