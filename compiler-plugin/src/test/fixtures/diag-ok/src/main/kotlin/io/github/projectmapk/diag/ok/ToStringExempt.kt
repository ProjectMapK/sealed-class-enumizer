package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-44: toString は手動宣言・継承具象とも MEMBER_CONFLICT の対象外

@Enumize
sealed interface NmTs

// Any 以外の具象 toString を持つ継承元
abstract class NmTsBase {
    override fun toString(): String = "custom-inherited"
}

// kind が継承経路上の具象 toString を持つ形
object NmTsLeaf : NmTsBase(), NmTs

// kind companion の手動 toString 宣言
class NmTsMan(val v: Int) : NmTs {
    companion object {
        override fun toString(): String = "manual!"
    }
}
