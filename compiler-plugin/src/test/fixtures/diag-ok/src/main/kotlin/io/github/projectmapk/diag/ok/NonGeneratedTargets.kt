package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize
import kotlin.reflect.KClass

// docs/test/ケース04-診断.md DIA-45: 生成先と異なる位置・検査名集合外の宣言は MEMBER_CONFLICT 非発火

// 末端 class 本体の enumizedClass 宣言（生成先は companion）
@Enumize
sealed interface OkM {
    data class Foo(val v: Int) : OkM {
        val enumizedClass: KClass<Foo> get() = Foo::class
    }
}

// kind の enumishCompanion override は許容
@Enumize
sealed interface OkC {
    data object Bar : OkC {
        override val enumishCompanion: OkC.Enumish.Companion get() = OkC.Enumish
    }
}

// kind companion の手動 asEnumish（検査名集合外の過負荷）
@Enumize
sealed interface OkKca {
    class Leaf(val v: Int) : OkKca {
        companion object {
            fun asEnumish(tag: String): String = tag
        }
    }
}
