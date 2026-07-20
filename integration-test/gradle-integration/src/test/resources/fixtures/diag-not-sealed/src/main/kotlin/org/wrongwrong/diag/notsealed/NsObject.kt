package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-003: @Enumize を object に付与 → ENUMIZE_NOT_SEALED（object は継承者を持てず階層が定義不能）
@Enumize
object NsObject
