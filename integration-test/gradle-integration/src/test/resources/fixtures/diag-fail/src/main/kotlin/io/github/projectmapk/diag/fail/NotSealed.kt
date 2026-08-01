package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-01: K1 の負値（非 sealed 分類子）全種別への付与 → ENUMIZE_NOT_SEALED。
// 非 object 宣言はアノテーション行・object 宣言は object キーワード行に報告される

@Enumize
enum class NsEnum { HELP, VERSION }

@Enumize
annotation class NsAnnotation

@Enumize
object NsObject

@Enumize
data object NsDataObject

@Enumize
open class NsOpenClass

@Enumize
abstract class NsAbstractClass

// （修飾なし）final class
@Enumize
class NsFinalClass

@Enumize
data class NsDataClass(val v: Int)

// value class は sealed 不能
@Enumize
@JvmInline
value class NsValueClass(val v: Int)

@Enumize
interface NsInterface

@Enumize
fun interface NsFunInterface {
    fun handle(x: Int): Int
}

// inner は sealed 不能の帰結
class NsInnerHost {
    @Enumize
    inner class NsInner
}
