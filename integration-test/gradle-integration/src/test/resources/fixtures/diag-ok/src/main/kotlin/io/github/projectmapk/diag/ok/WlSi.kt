package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-37/39: label メンバー宣言の ES 警告と非対象（Named は label 以外）
@Enumize
sealed interface WlSi {
    data class Tagged(val label: String) : WlSi

    data class Named(val name: String) : WlSi

    class Fn : WlSi {
        fun label(): String = "fn"

        companion object
    }
}
