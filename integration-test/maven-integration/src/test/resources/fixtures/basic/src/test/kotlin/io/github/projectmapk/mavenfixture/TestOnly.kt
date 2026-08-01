package io.github.projectmapk.mavenfixture

import io.github.projectmapk.sealedClassEnumizer.Enumize

// test compilation にもプラグインが適用されることの観測用（生成 API を test 側の階層で使う）
@Enumize
sealed interface TestOnly {
    data object OnlyLeaf : TestOnly
}
