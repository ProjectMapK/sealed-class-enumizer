package org.wrongwrong.fixtures.baseimpl

import org.wrongwrong.fixtures.SI
import org.wrongwrong.sealedClassEnumizer.Enumish
import org.wrongwrong.sealedClassEnumizer.Enumized
import kotlin.reflect.KClass

// 基底 Enumish（runtime-api）の手動実装は無制約（TC-BOX-079）。
// 非 sealed の public interface のため任意の場所で実装でき、どの @Enumize 階層の entries にも現れない。
// enumishCompanion / enumizedClass は型を満たす値を返せばよい（ここでは SI 階層のものを借用）
object FreeAgent : Enumish {
    override val label: String get() = "FreeAgent"

    override val enumishCompanion: Enumish.Companion<Enumish> get() = SI.Enumish

    override val enumizedClass: KClass<out Enumized<*>> get() = SI.Foo::class
}
