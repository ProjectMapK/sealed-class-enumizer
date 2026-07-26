package org.wrongwrong.diag.ok

import kotlin.reflect.KClass
import org.wrongwrong.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-45: 末端 class 本体の enumizedClass 宣言（生成先は companion）→ 非発火
@Enumize
sealed interface OkM {
    data class Foo(val v: Int) : OkM {
        val enumizedClass: KClass<Foo> get() = Foo::class
    }
}
