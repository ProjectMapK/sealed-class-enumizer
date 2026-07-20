package org.wrongwrong.sweep.fail

// TC-VIS-056: public 関数が internal companion 型を返り値に公開 → EXPOSED_FUNCTION_RETURN_TYPE。
// asEnumish の返り値型を素直に具体型へすると起きるエラーの手書き模擬（規則 2 フォールバックの根拠）
fun swExposeReturn(): SwExpose.Companion = SwExpose
