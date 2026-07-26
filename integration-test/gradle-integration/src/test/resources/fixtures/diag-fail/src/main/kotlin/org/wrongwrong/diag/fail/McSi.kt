package org.wrongwrong.diag.fail

import kotlin.reflect.KClass
import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-41: 手動 kind companion（既定名）の label / enumizedClass 手動宣言 → MMC
@Enumize
sealed interface McSi {
    class Leaf(val v: Int) : McSi {
        companion object {
            override val label: String get() = "manual"

            override val enumizedClass: KClass<out McSi> get() = Leaf::class
        }
    }
}
