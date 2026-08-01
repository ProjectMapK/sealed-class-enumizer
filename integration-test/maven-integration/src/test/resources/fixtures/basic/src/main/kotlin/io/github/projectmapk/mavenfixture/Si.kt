package io.github.projectmapk.mavenfixture

import io.github.projectmapk.sealedClassEnumizer.Enumize

// labelCase 未指定（= PROJECT_DEFAULT）の階層。プロジェクト既定の指定がここへ届く
@Enumize
sealed interface Si {
    data object Bar : Si

    data class FooBar(val v: Int) : Si
}
