package io.github.projectmapk.icfix

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 非 sealed 外側クラス内のネスト基底（docs/test/ケース06-ビルド動態.md BLD-01 の
// $EntriesHolder 帰属観測のネスト基底形。SI / TI と独立した第 3 の階層）
class NbHost {
    @Enumize
    sealed interface NB {
        data object N1 : NB
    }
}
