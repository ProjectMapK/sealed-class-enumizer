package io.github.projectmapk.fixtures.reentry

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 初期化再入（docs/test/ケース01-生成と実行時API.md API-45 の valueOf 経路）:
// companion（kind）の初期化子から valueOf を参照する構成
@Enumize
sealed interface ReValueOf {
    class Leaf(val v: Int) : ReValueOf {
        companion object {
            val eager: Any = ReValueOf.Enumish.valueOf("Leaf")
        }
    }
}
