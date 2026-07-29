package io.github.projectmapk.sealedClassEnumizer

import kotlin.reflect.KClass

// toString は宣言しない: interface での抽象再宣言は Any の実装では充足されず、
// 宣言しても充足の成立条件を増やすだけで利点が無い（docs/概要.md §2）
interface Enumish {
    val label: String
    val enumishCompanion: EnumishCompanion<Enumish>
    val enumizedClass: KClass<out Enumized<*>>
}
