package org.wrongwrong.sweep.tasfhead

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// 手動 Enumized への typealias を基底の supertype の頭に使い、エイリアスが階層より先に解決されない
// 配置（同一ファイル）に置いた形。プラグインの注入は raw 追跡による展開で抑止されるが、言語側は
// 別名を展開しないまま IR へ進みバックエンド ICE になる（docs/概要.md §7 の既知の制限。
// エイリアスが先に処理される別ファイル配置は sweep-typealias-head で成立する）
typealias SwSfAlias = Enumized<SwSfSi.Enumish>

@Enumize
sealed interface SwSfSi : SwSfAlias {
    data object L : SwSfSi
}
