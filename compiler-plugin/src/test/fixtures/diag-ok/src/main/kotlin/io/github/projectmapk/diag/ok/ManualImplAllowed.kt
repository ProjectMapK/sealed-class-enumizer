package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumish
import io.github.projectmapk.sealedClassEnumizer.EnumishCompanion
import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized
import kotlin.reflect.KClass

// docs/test/ケース04-診断.md DIA-58: 階層内手動実装・基底 Enumish 実装・kind companion 免除の
// 成立側は許容され MANUAL_IMPL_OUTSIDE_HIERARCHY / MSM / MC は発火しない

@Enumize
sealed interface OkManSi {
    data object R : OkManSi
}

// 階層内手動実装（末端 class の両実装）。
// Enumish 由来でも可視な label 手動宣言には ES 警告が出る（DIA-37）
class OkManLeaf(val v: Int) : OkManSi, OkManSi.Enumish {
    override val label: String get() = "OkManLeaf"

    override val enumizedClass: KClass<out OkManSi> get() = OkManLeaf::class
}

// 末端 class の companion による `: Enumish` 明示宣言
// （kind companion 免除の成立側・メンバーは生成が充足）
@Enumize
sealed interface OkKex {
    class Leaf(val v: Int) : OkKex {
        companion object : OkKex.Enumish
    }
}

// 基底 Enumish（runtime-api・非 sealed public）の自由実装は無制約
class NmRt : Enumish {
    override val label: String get() = "free"

    override val enumishCompanion: EnumishCompanion<Enumish> get() = error("unused")

    override val enumizedClass: KClass<out Enumized<*>> get() = error("unused")
}
