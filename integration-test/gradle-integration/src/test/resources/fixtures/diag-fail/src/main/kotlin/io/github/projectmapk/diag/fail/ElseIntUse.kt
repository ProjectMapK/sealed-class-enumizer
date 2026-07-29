package io.github.projectmapk.diag.fail

// else 必須の位置依存 変種 1: 基底本体スコープの外からは private ネスト kind を名指しできず、
// else 無し kind-when は網羅不成立（言語エラー）
internal fun elseIntUse(si: ElseInt): String = when (si.asEnumish()) {
    ElseInt.A -> "a"
}
