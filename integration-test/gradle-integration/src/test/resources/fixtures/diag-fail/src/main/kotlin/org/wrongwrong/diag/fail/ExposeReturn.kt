package org.wrongwrong.diag.fail

// docs/test/ケース04-診断.md DIA-63: public 関数が internal companion 型を返す
// → EXPOSED_FUNCTION_RETURN_TYPE（規則 2 フォールバックの根拠）
fun exposeReturn(): Expose.Companion = Expose
