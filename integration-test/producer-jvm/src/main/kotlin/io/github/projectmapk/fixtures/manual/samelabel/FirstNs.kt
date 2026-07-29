package io.github.projectmapk.fixtures.manual.samelabel

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 階層間 label 閉域の片側（docs/test/ケース01-生成と実行時API.md API-43）。
// LABEL_CLASH は階層内でのみ判定され、跨階層の同名 label（Same）は許容される。
// トップレベル同名は言語の再宣言エラーになるため、同名末端は基底ネスト配置が必須
@Enumize
sealed interface FirstNs {
    data object Same : FirstNs
}
