package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-70 の非発火側: クラス supertype の検査対象は final のみ。
// open 具象は生成 override が勝ち、宣言種別交差（final 関数 label）と private は衝突しない

@Enumize
sealed interface OkCiSi

// open 具象 label を持つ階層外クラス
open class OkCiBase {
    open val label: String get() = "cls"
}

// companion 経由の open 具象継承
class OkCi(val v: Int) : OkCiSi {
    companion object : OkCiBase()
}

// 末端 object による open 具象の直接継承
object OkCiOpen : OkCiBase(), OkCiSi

// 同名でも宣言種別が交差する final 関数 label を持つ階層外クラス
open class OkCiFnBase {
    fun label(): String = "fn"
}

// 関数形 label は生成プロパティと JVM シグネチャが衝突しない
object OkCiFn : OkCiFnBase(), OkCiSi

// private の final label を持つ階層外クラス
open class OkCiPvBase {
    private val label: String = "pv"

    fun reveal(): String = label
}

// private の label は override 解決に参加しない
object OkCiPv : OkCiPvBase(), OkCiSi
