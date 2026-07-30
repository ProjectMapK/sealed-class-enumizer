package io.github.projectmapk.labelcase

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.LabelCase

// 具体指定（KEBAB_CASE）はプロジェクト既定（DSL の SNAKE_CASE）に勝つ
@Enumize(labelCase = LabelCase.KEBAB_CASE)
sealed interface Pinned {
    data object FooBar : Pinned
}
