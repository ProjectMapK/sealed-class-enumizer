package org.wrongwrong.sweep.xnuse

import org.wrongwrong.sweep.xn.SwXnMix
import org.wrongwrong.sweep.xn.SwXnPub

// TC-XM-010 負値側 / TC-VIS-036: 可視範囲の外側（別モジュール）からは internal kind SwXnSec を
// 名指しできず、else 無し kind-when は網羅不成立（must be exhaustive のコンパイルエラー）
fun useElse(x: SwXnMix): String = when (x.asEnumish()) {
    SwXnPub -> "pub"
}
