package io.github.projectmapk.diag.fail

import kotlin.reflect.KClass
import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-56: class 形 + Enumized 併記でも MIOH
class MiRogueCls : MiSi.Enumish, Enumized<MiSi.Enumish> {
    override val label: String get() = "roguecls"

    override val enumizedClass: KClass<out MiSi> get() = MiSi::class

    override fun asEnumish(): MiSi.Enumish = this
}
