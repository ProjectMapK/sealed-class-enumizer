package org.wrongwrong.diag.nm

import org.wrongwrong.sealedClassEnumizer.Enumish
import org.wrongwrong.sealedClassEnumizer.EnumishCompanion
import org.wrongwrong.sealedClassEnumizer.Enumized
import kotlin.reflect.KClass

// TC-DIAG-070: 基底 Enumish（runtime-api・非 sealed public）の直接実装は無制約 → 非発火
class NmRt : Enumish {
    override val label: String get() = "free"

    override val enumishCompanion: EnumishCompanion<Enumish> get() = error("unused")

    override val enumizedClass: KClass<out Enumized<*>> get() = error("unused")
}
