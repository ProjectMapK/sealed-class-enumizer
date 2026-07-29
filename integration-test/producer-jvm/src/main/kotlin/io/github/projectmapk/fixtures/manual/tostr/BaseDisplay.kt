package io.github.projectmapk.fixtures.manual.tostr

// Any 以外の具象 toString を提供する親クラス（docs/test/ケース01-生成と実行時API.md API-35 の継承元）
open class BaseDisplay {
    override fun toString(): String = "base-display"
}
