package io.github.projectmapk.diag.ok

import kotlin.reflect.KClass
import io.github.projectmapk.sealedClassEnumizer.Enumish
import io.github.projectmapk.sealedClassEnumizer.EnumishCompanion
import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-58: 基底 Enumish（runtime-api・非 sealed public）の自由実装は無制約
class NmRt : Enumish {
    override val label: String get() = "free"

    override val enumishCompanion: EnumishCompanion<Enumish> get() = error("unused")

    override val enumizedClass: KClass<out Enumized<*>> get() = error("unused")
}
