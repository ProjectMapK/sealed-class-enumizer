package org.wrongwrong.fixtures.widerleaf

import org.wrongwrong.sealedClassEnumizer.Enumize

// 基底より広い末端 + 自動生成 companion の基底（TC-VIS-030）。末端は AutoWide.kt
@Enumize internal sealed interface AutoBase
