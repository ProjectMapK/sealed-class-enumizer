package io.github.projectmapk.probe.alias

import io.github.projectmapk.sealedClassEnumizer.Enumize as Ez

// docs/test/ケース04-診断.md DIA-67: import 別名表記は述語（エイリアス展開前）に載らず生成が
// 走らないため ENUMIZE_ALIASED_ANNOTATION（Main からは参照しない）
@Ez
sealed interface AaIm {
    data object I1 : AaIm
}
