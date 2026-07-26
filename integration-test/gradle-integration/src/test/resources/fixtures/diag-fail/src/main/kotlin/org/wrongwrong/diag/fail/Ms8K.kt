package org.wrongwrong.diag.fail

import org.wrongwrong.sealedClassEnumizer.Enumish

// docs/test/ケース04-診断.md DIA-49 用の非適格 K（経路内具象実装 = 具象メンバーを持つ）
interface Ms8K : Enumish {
    override val label: String get() = "k8"
}
