package io.github.projectmapk.diag.fail

import kotlin.reflect.KClass
import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-40: 末端 object の label / enumizedClass / asEnumish 手動宣言
// → 各 ENUMIZE_MEMBER_CONFLICT + メンバー名（末端 object は kind 自身のため ES 警告は不発）
@Enumize
sealed interface Mc1Si {
    data object Bad : Mc1Si {
        override val label: String get() = "manual"

        override val enumizedClass: KClass<out Mc1Si> get() = Mc1Si::class

        override fun asEnumish(): Mc1Si.Enumish = this
    }
}
