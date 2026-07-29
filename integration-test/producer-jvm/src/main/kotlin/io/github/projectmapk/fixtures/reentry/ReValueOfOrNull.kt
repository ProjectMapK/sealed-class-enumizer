package io.github.projectmapk.fixtures.reentry

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 初期化再入（docs/test/ケース01-生成と実行時API.md API-45 の valueOfOrNull 経路）:
// companion（kind）の初期化子から valueOfOrNull を参照する構成
@Enumize
sealed interface ReValueOfOrNull {
    class Leaf(val v: Int) : ReValueOfOrNull {
        companion object {
            val eager: Any? = ReValueOfOrNull.Enumish.valueOfOrNull("Leaf")
        }
    }
}
