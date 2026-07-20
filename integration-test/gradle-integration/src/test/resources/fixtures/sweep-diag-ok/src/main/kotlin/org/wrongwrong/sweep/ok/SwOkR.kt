package org.wrongwrong.sweep.ok

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-MAN-041: Enumish という名前のメンバー（プロパティ = 分類子でない）は生成ネスト名 Enumish と
// 名前空間が異なり ENUMIZE_RESERVED_NAME_CLASH は非発火。生成 Enumish は通常どおり作られる
@Enumize
sealed interface SwOkR {
    val Enumish: Int get() = 0

    data object L : SwOkR
}
