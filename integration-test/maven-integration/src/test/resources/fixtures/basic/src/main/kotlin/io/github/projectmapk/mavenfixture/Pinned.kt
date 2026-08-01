package io.github.projectmapk.mavenfixture

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.LabelCase

// 具体指定はプロジェクト既定にも実行単位の指定にも勝つ
@Enumize(labelCase = LabelCase.KEBAB_CASE)
sealed interface Pinned {
    data object FooBar : Pinned
}
