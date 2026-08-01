package io.github.projectmapk.diag.ok

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-37/38/39: 可視 label メンバーの宣言・継承で
// ENUMIZE_EXTENSION_SHADOWED 警告が出る形と、除外される 3 形を同居させる。
// 階層内手動実装 leaf の Enumish 由来 label（発火側）は ManualImplAllowed.kt が担う

// DIA-37: 末端 class 本体の label 宣言（Tagged = ctor プロパティ・Fn = 関数形）で ES。
// DIA-39: label 以外の名前（Named）は非対象
@Enumize
sealed interface WlSi {
    data class Tagged(val label: String) : WlSi

    data class Named(val name: String) : WlSi

    class Fn : WlSi {
        fun label(): String = "fn"

        companion object
    }
}

// DIA-37: 基底自身の label 宣言で ES。
// DIA-38: 継承末端 class（C）はクラス位置 + 宣言元 FQN で ES。
// DIA-39: 末端 object（L）の継承は除外
@Enumize
sealed interface Wl2Si {
    val label: String get() = "base"

    data object L : Wl2Si

    data class C(val v: Int) : Wl2Si
}

// DIA-38: 階層外 interface からの default 継承（クラス位置 + 宣言元 FQN）
@Enumize
sealed interface Wl3Si

interface Wl3Named {
    val label: String get() = "named"
}

class Wl3Leaf : Wl3Si, Wl3Named

// DIA-39: private の label 宣言（可視条件の偽側）は非発火
@Enumize
sealed interface WlPriv {
    class Q : WlPriv {
        private val label: String = "q"

        companion object
    }
}
