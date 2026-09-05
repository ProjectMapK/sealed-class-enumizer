package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-29: 末端 class の companion 自身が末端を兼ねる形
// → ENUMIZE_COMPANION_LEAF_CONFLICT（報告位置 = companion 宣言。判定は階層不問）

@Enumize
sealed interface ClcSi

// 既定名 companion
class ClcHost(val v: Int) : ClcSi {
    companion object : ClcSi
}

@Enumize
sealed interface Clc2Si

// 名前つき companion でも同様
class Clc2Host(val v: Int) : Clc2Si {
    companion object Named : Clc2Si
}

// ホスト側の所属先
@Enumize
sealed interface Clc3SiA

// companion の所属先
@Enumize
sealed interface Clc3SiB

// companion が「別階層の」末端でも発火する
class Clc3Host(val v: Int) : Clc3SiA {
    companion object : Clc3SiB
}
