package org.wrongwrong.fixtures.tostring

// Any 以外の具象 toString を提供する親クラス（TC-LEAF-060 / TC-BOX-045 の継承元）
open class BaseDisplay {
    override fun toString(): String = "base-display"
}
