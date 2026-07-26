package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-01: object への付与 → ENUMIZE_NOT_SEALED（object 宣言は object キーワード行に報告）
@Enumize
object NsObject
