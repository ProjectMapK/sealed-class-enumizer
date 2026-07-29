package io.github.projectmapk.diag.fail

import kotlin.reflect.KClass
import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-41: 手動 kind companion（既定名）の label / enumizedClass 手動宣言 → MC
@Enumize
sealed interface Mc3Si {
    class Leaf(val v: Int) : Mc3Si {
        companion object {
            override val label: String get() = "manual"

            override val enumizedClass: KClass<out Mc3Si> get() = Leaf::class
        }
    }
}
