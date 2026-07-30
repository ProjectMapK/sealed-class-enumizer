package io.github.projectmapk.fixtures.label

import io.github.projectmapk.sealedClassEnumizer.EnumishLabel
import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.LabelCase

// 階層個別の labelCase 指定と明示 label の優先関係（docs/test/ケース01-生成と実行時API.md API-53〜API-54）。
// 変換は enum 末端の kind を含む全末端へ一律適用され、data object の toString は言語合成のまま
// label と乖離する（docs/概要.md §4）
@Enumize(labelCase = LabelCase.UPPER_SNAKE_CASE)
sealed interface Cased {
    data class FooBar(val v: Int) : Cased

    data object HTTPServer : Cased

    // 明示 label はケース変換を受けず、この値がそのまま最終 label になる
    @EnumishLabel("legacy-name") data class Renamed(val v: Int) : Cased

    enum class Builtin : Cased {
        HELP
    }
}
