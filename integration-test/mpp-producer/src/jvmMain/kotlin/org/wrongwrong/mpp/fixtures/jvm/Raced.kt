package org.wrongwrong.mpp.fixtures.jvm

import org.wrongwrong.sealedClassEnumizer.Enumize

// JVM 実マルチスレッドでの entries 初回アクセス競合フィクスチャ（docs/テストケース管理.md
// TC-MPP-052）。初回アクセスを競わせるため、この階層に触れるのは LazyRaceTest だけである
@Enumize
sealed interface Raced {
    data object R1 : Raced

    data class R2(val v: Int) : Raced
}
