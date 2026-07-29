package io.github.projectmapk.fixtures.manual.impl

import io.github.projectmapk.sealedClassEnumizer.Enumish
import io.github.projectmapk.sealedClassEnumizer.EnumishCompanion
import io.github.projectmapk.sealedClassEnumizer.Enumized
import kotlin.reflect.KClass

// 基底 Enumish（runtime-api）の自由実装（階層外。docs/test/ケース01-生成と実行時API.md API-42）。
// 非 sealed の public interface のため任意の場所で実装でき、どの @Enumize 階層の entries にも現れない。
// enumishCompanion / enumizedClass は型を満たす値を返せばよい（ここでは WithManual 階層のものを借用）
object FreeAgent : Enumish {
    override val label: String
        get() = "FreeAgent"

    override val enumishCompanion: EnumishCompanion<Enumish>
        get() = WithManual.Enumish

    override val enumizedClass: KClass<out Enumized<*>>
        get() = WithManual.Real::class
}
