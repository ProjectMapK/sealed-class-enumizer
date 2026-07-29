package io.github.projectmapk.diag.ok

// docs/test/ケース04-診断.md DIA-44 用: Any 以外の具象 toString を持つ継承元
abstract class NmTsBase {
    override fun toString(): String = "custom-inherited"
}
