package org.wrongwrong.sweep.fail

import org.wrongwrong.sealedClassEnumizer.Enumize

// TC-MAN-057(a) 用の基底（生成 Enumish は sealed = V1。手動実装は同一パッケージ制約に従う）
@Enumize
sealed interface SwRogueSi {
    data object R1 : SwRogueSi
}
