package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-65 用の基底。
// 別パッケージ側の末端宣言は failother/FarLeaf.kt が担う（別パッケージ配置が本ケースの成立条件）
@Enumize
sealed interface FarSi {
    data object Near : FarSi
}
