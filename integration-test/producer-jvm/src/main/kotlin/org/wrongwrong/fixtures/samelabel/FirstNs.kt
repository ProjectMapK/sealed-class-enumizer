package org.wrongwrong.fixtures.samelabel

import org.wrongwrong.sealedClassEnumizer.Enumize

// 2 階層間の label 分離（TC-BOX-074）。LABEL_CLASH は階層内でのみ判定され、
// 跨階層の同名 label（Same）は許容される（このファイルがコンパイルできること自体が非発火の証明）
@Enumize
sealed interface FirstNs {
    data object Same : FirstNs
}
