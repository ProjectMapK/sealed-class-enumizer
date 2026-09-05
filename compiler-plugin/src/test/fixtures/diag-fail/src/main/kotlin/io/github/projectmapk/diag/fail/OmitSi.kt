package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-33 用の基底
@Enumize
sealed interface OmitSi

// DIA-33: 手動 companion（`: Enumish` 明示宣言）の生成対象メンバー省略
// → 生成がメンバーを充足しエラー無し（COMPANION_REQUIRED 診断も abstract 未実装エラーも出ない）
class OmitHost : OmitSi {
    companion object : OmitSi.Enumish
}
