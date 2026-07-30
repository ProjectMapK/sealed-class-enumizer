package io.github.projectmapk.fixtures.label

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.LabelCase

// PROJECT_DEFAULT の明示指定はプロジェクト既定へ解決される（docs/test/ケース01-生成と実行時API.md API-55）。
// 本モジュールは DSL 未設定のため、gradle プラグインの convention（AS_DECLARED）が届く
@Enumize(labelCase = LabelCase.PROJECT_DEFAULT)
sealed interface ProjectDefaulted {
    data object AlphaBeta : ProjectDefaulted
}
