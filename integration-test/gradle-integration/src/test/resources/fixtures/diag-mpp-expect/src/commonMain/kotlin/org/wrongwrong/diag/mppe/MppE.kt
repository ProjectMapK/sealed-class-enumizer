package org.wrongwrong.diag.mppe

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-009: expect 宣言への @Enumize → ENUMIZE_ON_EXPECT（actual 毎に階層が変わりうるための v1 制限）
@Enumize
expect sealed interface MppE
