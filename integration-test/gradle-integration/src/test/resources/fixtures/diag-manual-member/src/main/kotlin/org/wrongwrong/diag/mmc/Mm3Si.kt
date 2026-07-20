package org.wrongwrong.diag.mmc

import org.wrongwrong.sealedClassEnumizer.Enumize
import kotlin.reflect.KClass

// TC-DIAG-046: kind の enumizedClass 手動宣言 → ENUMIZE_MANUAL_MEMBER_CONFLICT
@Enumize
sealed interface Mm3Si {
    data object Bad : Mm3Si {
        override val enumizedClass: KClass<out Mm3Si> get() = Mm3Si::class
    }
}
