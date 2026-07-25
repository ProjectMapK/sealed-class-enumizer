package org.wrongwrong.fixtures.privatewide

import org.wrongwrong.sealedClassEnumizer.Enumize

// private トップレベル基底 + 同一ファイルの public 末端（TC-VIS-061 = E-2 の private 基底版）。
// private 基底はファイル内でのみ可視のため、基底より広い末端は同一ファイル配置が必須になる
// （このファイルのみ 1 ファイル 1 クラス規約の例外。ケースの意味上の要請）
@Enumize private sealed interface PrivateWide

// 基底（private）より広い public 末端。露出検査は interface 実装に働かないため成立する（エッジ §1.1 #2）
class WideLeaf(val v: Int) : PrivateWide {
    companion object
}
