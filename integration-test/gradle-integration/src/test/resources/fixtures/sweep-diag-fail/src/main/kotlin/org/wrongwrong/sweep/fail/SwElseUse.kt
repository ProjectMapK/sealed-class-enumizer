package org.wrongwrong.sweep.fail

// TC-VIS-062: 同一モジュールでも基底本体スコープの外（別ファイル）からは private ネスト kind を
// 名指しできず、else 無し kind-when は網羅不成立（else 省略の判定は可視範囲=スコープ単位）
internal fun swElseUse(si: SwElse): String = when (si.asEnumish()) {
    SwElse.A -> "a"
}
