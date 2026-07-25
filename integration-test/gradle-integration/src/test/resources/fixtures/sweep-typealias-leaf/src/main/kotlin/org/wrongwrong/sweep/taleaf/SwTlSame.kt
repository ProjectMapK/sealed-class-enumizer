package org.wrongwrong.sweep.taleaf

import org.wrongwrong.sealedClassEnumizer.Enumize

// 同一ファイル配置の別名版（SwTlSi は別ファイルの typealias を使う）。生成型への typealias は
// エイリアスが階層より先に解決されない配置では解決済み supertype でも展開されないため、注入抑止の照合は
// raw 追跡（エイリアス自身のスコープでの名前解決）へ落ちる
typealias SwTlSameAlias = SwTlSameSi.Enumish

@Enumize
sealed interface SwTlSameSi {
    data object L : SwTlSameSi, SwTlSameAlias
}
