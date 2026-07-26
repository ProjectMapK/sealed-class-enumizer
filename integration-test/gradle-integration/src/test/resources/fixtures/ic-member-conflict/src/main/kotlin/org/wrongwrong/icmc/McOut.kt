package org.wrongwrong.icmc

// docs/test/ケース06-ビルド動態.md BLD-47 用: label を open⇄final トグルする階層外クラス
open class McOut {
    open val label: String get() = "cls"
}
