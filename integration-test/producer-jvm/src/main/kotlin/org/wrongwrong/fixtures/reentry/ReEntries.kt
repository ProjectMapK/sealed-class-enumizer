package org.wrongwrong.fixtures.reentry

import org.wrongwrong.sealedClassEnumizer.Enumize

// 初期化再入の禁止（TC-BOX-052）: kind（末端 object）の初期化子から階層の entries を参照する構成。
// docs/概要.md §6 は遅延初期化の再入で StackOverflow になりうるとする（実挙動はテストで観測）
@Enumize
sealed interface ReEntries {
    object Boom : ReEntries {
        val seenDuringInit: List<Any?> = ReEntries.Enumish.entries
    }
}
