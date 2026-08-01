package io.github.projectmapk.sweep.md

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-12 用の基底（commonMain に label 衝突する 2 末端を持つ）
@Enumize
sealed interface SwMd

// label 衝突の当事者 A（common ソースセット）
class SwMdOuterA {
    object Dup : SwMd
}

// label 衝突の当事者 B（common ソースセット）
class SwMdOuterB {
    object Dup : SwMd
}
