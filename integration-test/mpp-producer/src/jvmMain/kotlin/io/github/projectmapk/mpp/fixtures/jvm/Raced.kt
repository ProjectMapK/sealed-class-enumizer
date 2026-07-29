package io.github.projectmapk.mpp.fixtures.jvm

import io.github.projectmapk.sealedClassEnumizer.Enumize

// JVM 実マルチスレッドでの entries 初回アクセス競合フィクスチャ（docs/test/ケース05-境界横断.md
// XMP-46）。初回アクセスを競わせるため、この階層に触れるのは LazyRaceTest だけである
@Enumize
sealed interface Raced {
    data object R1 : Raced

    data class R2(val v: Int) : Raced
}
