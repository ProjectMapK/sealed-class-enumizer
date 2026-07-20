package org.wrongwrong.sweep.ok

import org.wrongwrong.sealedClassEnumizer.Enumize
import kotlin.reflect.KClass

// TC-MAN-064: 末端 class 本体の enumizedClass 宣言（生成先は companion のため無関係な独自プロパティ）
// → ENUMIZE_MANUAL_MEMBER_CONFLICT 非発火・enumizedClass は拡張でないため EXTENSION_SHADOWED も非対象
@Enumize
sealed interface SwOkM {
    data class Foo(val v: Int) : SwOkM {
        val enumizedClass: KClass<Foo> get() = Foo::class
    }
}
