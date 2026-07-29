package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-49: 非適格 K（狭い可視性）でも v1 は一律 MSM
// （interface supertype は露出検査の対象外のため言語エラーには掛からない）
@Enumize
sealed interface Ms7Si : Enumized<Ms7K> {
    data object L7 : Ms7Si
}
