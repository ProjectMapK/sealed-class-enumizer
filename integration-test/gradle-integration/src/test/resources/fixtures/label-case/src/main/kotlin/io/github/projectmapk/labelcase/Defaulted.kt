package io.github.projectmapk.labelcase

import io.github.projectmapk.sealedClassEnumizer.Enumize

// labelCase 未指定（= PROJECT_DEFAULT）の階層。DSL の SNAKE_CASE が届く
@Enumize
sealed interface Defaulted {
    data object BazQux : Defaulted

    data class FooBar(val v: Int) : Defaulted
}
