package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-01: object への付与 → ENUMIZE_NOT_SEALED（object 宣言は object キーワード行に報告）
@Enumize
object NsObject
