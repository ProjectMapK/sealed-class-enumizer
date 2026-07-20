package org.wrongwrong.diag.notsealed

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-DIAG-001: @Enumize を enum class に付与 → ENUMIZE_NOT_SEALED（docs/コンパイラプラグイン設計01.md §7.2）
@Enumize
enum class NsEnum { HELP, VERSION }
