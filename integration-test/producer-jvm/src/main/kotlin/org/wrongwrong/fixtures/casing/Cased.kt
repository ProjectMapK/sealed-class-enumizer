package org.wrongwrong.fixtures.casing

import org.wrongwrong.sealedClassEnumizer.Enumize

// 大文字小文字を区別する UTF-16 序数の境界（TC-ORD-008）: 第 1 文字 'A'(65) < 'a'(97)・第 2 文字 'B'(66) < 'b'(98)。
// TC-ORD-008 の配置（AB/Ab/aB/ab）は大文字小文字のみが異なるため、Windows 等の大文字小文字非区別
// ファイルシステムでは class ファイル名（Cased$AB.class / Cased$Ab.class）が衝突し実行できない
// （java.lang.NoClassDefFoundError (wrong name)。プラグインではなく JVM / FS の環境制約）。
// 末尾に数字を足して FS 上の一意性を保ちつつ、比較が数字に到達する前に大小境界で決着する名前へ変更している
@Enumize
sealed interface Cased {
    @Suppress("ClassName")
    data object ab4 : Cased

    @Suppress("ClassName")
    data object aB3 : Cased

    data object Ab2 : Cased

    data object AB1 : Cased
}
