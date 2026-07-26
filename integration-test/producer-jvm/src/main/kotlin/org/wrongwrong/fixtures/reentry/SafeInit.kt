package org.wrongwrong.fixtures.reentry

import org.wrongwrong.sealedClassEnumizer.Enumize

// 初期化再入の非発火 near-miss（docs/test/ケース01-生成と実行時API.md API-46）:
// asEnumish / label / enumishCompanion は EntriesHolder の lazy に触れないため、
// kind の初期化子から参照しても安全
@Enumize
sealed interface SafeInit {
    data object Probe : SafeInit {
        val initLabel: String = asEnumish().label

        val initCompanion: Any = enumishCompanion
    }
}
