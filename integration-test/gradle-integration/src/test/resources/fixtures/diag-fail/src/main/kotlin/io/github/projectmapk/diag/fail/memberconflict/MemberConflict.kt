package io.github.projectmapk.diag.fail.memberconflict

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized
import kotlin.reflect.KClass

// docs/test/ケース04-診断.md DIA-40〜43: 生成対象メンバー（label / enumizedClass / asEnumish）と
// 衝突する手動宣言・継承具象 → ENUMIZE_MEMBER_CONFLICT + メンバー名。
// 末端 object の手動宣言形は ES 非発火の観測を伴うため Mc1Si.kt が担う

// --- DIA-40: 判定は callable 名単位（宣言種別交差・引数付き過負荷でも MC） ---

@Enumize
sealed interface Mc2Si {
    data object Fn : Mc2Si {
        fun label(): String = "fn"
    }

    data class Ctor(val asEnumish: Int) : Mc2Si {
        companion object
    }

    data object Over : Mc2Si {
        fun asEnumish(tag: String): String = tag
    }
}

// --- DIA-41: 手動 kind companion（既定名）の手動宣言 ---

@Enumize
sealed interface Mc3Si {
    class Leaf(val v: Int) : Mc3Si {
        companion object {
            override val label: String get() = "manual"

            override val enumizedClass: KClass<out Mc3Si> get() = Leaf::class
        }
    }
}

// --- DIA-41: enum 末端の kind companion の label 手動宣言 ---

@Enumize
sealed interface Mc4Enum {
    enum class Builtin : Mc4Enum {
        HELP,
        ;

        companion object {
            override val label: String get() = "custom"
        }
    }
}

// --- DIA-42: 末端 class / interface の asEnumish 手動宣言 ---

@Enumize
sealed interface Mc5Si

class Mc5Leaf : Mc5Si {
    companion object

    override fun asEnumish(): Mc5Si.Enumish = throw UnsupportedOperationException()
}

@Enumize
sealed interface Mc6Si {
    interface Custom : Mc6Si {
        override fun asEnumish(): Mc6Si.Enumish = Companion

        companion object
    }
}

// --- DIA-43: 階層外 interface からの同名具象 default 継承（label / asEnumish / enumizedClass の 3 変種） ---

@Enumize
sealed interface Mc7Si

interface Mc7Named {
    val label: String get() = "named"
}

object Mc7Leaf : Mc7Si, Mc7Named

@Enumize
sealed interface Mc8Si {
    // default 実装の返り値を兼ねる末端
    data object Real : Mc8Si
}

interface Mc8Manual : Enumized<Mc8Si.Enumish> {
    override fun asEnumish(): Mc8Si.Enumish = Mc8Si.Real
}

data class Mc8Leaf(val v: Int) : Mc8Si, Mc8Manual {
    companion object
}

@Enumize
sealed interface Mc9Si

interface Mc9Prov {
    val enumizedClass: KClass<out Mc9Si> get() = Mc9Si::class
}

object Mc9Leaf : Mc9Si, Mc9Prov
