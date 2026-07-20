package org.wrongwrong.fixtures.mixedorder

import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/コンパイラプラグイン設計00.md §6.1 の Kotlin 2.4.0 実測形の再現（TC-ORD-004/005/007・TC-BOX-005）。
// ネスト末端 S.Aaa / Box.Bbb・トップレベル末端 Mmm / Zzz / aLower の混在で、
// entries = [Box.Bbb, Mmm, S.Aaa, Zzz, aLower]（FQN 序数順。単純名順・宣言順・大小無視のいずれでもない）
@Enumize
sealed interface S {
    data object Aaa : S
}
