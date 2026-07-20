package org.wrongwrong.diag.nm

// TC-DIAG-083 用: Any 以外の具象 toString を持つ継承元
abstract class NmTsBase {
    override fun toString(): String = "custom-inherited"
}
