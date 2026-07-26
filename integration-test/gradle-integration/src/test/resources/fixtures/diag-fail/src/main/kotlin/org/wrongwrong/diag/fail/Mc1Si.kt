package org.wrongwrong.diag.fail

import kotlin.reflect.KClass
import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-40: 末端 object の label / enumizedClass / asEnumish 手動宣言
// → 各 ENUMIZE_MANUAL_MEMBER_CONFLICT + メンバー名（末端 object は kind 自身のため ES 警告は不発）
@Enumize
sealed interface Mm1Si {
    data object Bad : Mm1Si {
        override val label: String get() = "manual"

        override val enumizedClass: KClass<out Mm1Si> get() = Mm1Si::class

        override fun asEnumish(): Mm1Si.Enumish = this
    }
}
