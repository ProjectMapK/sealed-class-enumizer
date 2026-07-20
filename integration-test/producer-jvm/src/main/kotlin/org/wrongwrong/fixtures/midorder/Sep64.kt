package org.wrongwrong.fixtures.midorder

import org.wrongwrong.sealedClassEnumizer.Enumize

// セパレータ境界 '.'(46) < '0'(48) を踏むフィクスチャ（TC-ORD-064）。
// 継承者比較 "Sep64.Foo" vs "Foo0" は 'S' > 'F' で Foo0 が先 → entries = [Foo0, Bar]。
// ネスト末端 Bar の FQN（p.Sep64.Foo.Bar）が中間 Foo の FQN を接頭辞に持つ性質の固定
@Enumize
sealed interface Sep64 {
    sealed interface Foo : Sep64 {
        data object Bar : Foo
    }
}
