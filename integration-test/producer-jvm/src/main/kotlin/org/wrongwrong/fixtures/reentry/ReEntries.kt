package org.wrongwrong.fixtures.reentry

import org.wrongwrong.sealedClassEnumizer.Enumize

// 初期化再入（docs/test/ケース01-生成と実行時API.md API-45 の entries 経路）:
// kind（末端 object）の初期化子から階層の entries を参照する構成。docs/概要.md §6 の禁止事項。
// JVM 実測では SOE にならず、object の INSTANCE 早期代入により再入した createEntries が完了する
// （初期化子の二重実行）。seenDuringInit は初期化中に観測した entries（確定後とは別 List インスタンス・内容同一）
@Enumize
sealed interface ReEntries {
    object Boom : ReEntries {
        val seenDuringInit: List<ReEntries.Enumish> = ReEntries.Enumish.entries
    }
}
