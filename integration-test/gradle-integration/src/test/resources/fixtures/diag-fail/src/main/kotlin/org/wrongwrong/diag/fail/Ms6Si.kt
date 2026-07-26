package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// docs/test/ケース04-診断.md DIA-49: 非適格 K（経路内自階層型）でも v1 は一律 MSM
// （K は interface 末端経由で構成し MIOH 非併発 = 1 宣言 1 診断の維持）
@Enumize
sealed interface Ms6Si : Enumized<Ms6K> {
    interface LeafI : Ms6Si
}
