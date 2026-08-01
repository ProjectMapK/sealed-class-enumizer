package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized
import kotlin.reflect.KClass

// docs/test/ケース04-診断.md DIA-56: 階層外からの生成 Enumish 直接実装
// → ENUMIZE_MANUAL_IMPL_OUTSIDE_HIERARCHY + 基底 FQN（報告位置 = supertype ref）。
// 同一 module 別 pkg 形は failother/AlienPkg.kt が担う

@Enumize
sealed interface MiSi {
    data object Ok : MiSi
}

// 階層外 object の直接実装
object MiRogue : MiSi.Enumish {
    override val label: String get() = "rogue"

    override val enumizedClass: KClass<out MiSi> get() = MiSi::class
}

// class 形 + Enumized 併記でも MIOH
class MiRogueCls : MiSi.Enumish, Enumized<MiSi.Enumish> {
    override val label: String get() = "roguecls"

    override val enumizedClass: KClass<out MiSi> get() = MiSi::class

    override fun asEnumish(): MiSi.Enumish = this
}

// 中間 sealed の companion による実装 → kind companion 免除の不成立側（MIOH 発火）
sealed interface MiMid : MiSi {
    companion object : MiSi.Enumish {
        override val label: String get() = "mid"

        override val enumizedClass: KClass<out MiSi> get() = MiMid::class
    }
}
