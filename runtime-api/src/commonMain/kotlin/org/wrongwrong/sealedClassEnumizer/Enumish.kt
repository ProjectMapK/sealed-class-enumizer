package org.wrongwrong.sealedClassEnumizer

import kotlin.reflect.KClass

// toString は宣言しない: interface での抽象再宣言は Any の実装では充足されず、
// 宣言しても充足の成立条件を増やすだけで利点が無い（docs/概要.md §2）
interface Enumish {
    val label: String
    val enumishCompanion: Companion<Enumish>
    val enumizedClass: KClass<out Enumized<*>>

    interface Companion<out T : Enumish> {
        val entries: List<T>
        fun valueOf(value: String): T
        fun valueOfOrNull(value: String): T?
    }
}
