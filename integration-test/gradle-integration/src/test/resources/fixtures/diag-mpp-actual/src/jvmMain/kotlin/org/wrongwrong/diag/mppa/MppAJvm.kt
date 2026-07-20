package org.wrongwrong.diag.mppa

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-010: actual 宣言への @Enumize → ENUMIZE_ON_ACTUAL（expect-actual マッチングとの両立が未検証のための v1 制限）
@Enumize
actual sealed interface MppA
