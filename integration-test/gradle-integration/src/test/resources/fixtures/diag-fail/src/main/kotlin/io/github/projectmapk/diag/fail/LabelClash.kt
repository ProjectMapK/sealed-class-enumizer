package io.github.projectmapk.diag.fail

import io.github.projectmapk.sealedClassEnumizer.Enumize

// docs/test/ケース04-診断.md DIA-34/35: 同一階層内で最終 label が衝突する末端対
// → 両当事者へ ENUMIZE_LABEL_CLASH + 相手 FQN。
// 参照不能末端の同居（抑止も誘発もしないこと）は Lc4Priv.kt が担う

// --- 同一単純名の末端対（DIA-34） ---

@Enumize
sealed interface LcSi

class LcOuter1 {
    object Foo : LcSi
}

class LcOuter2 {
    object Foo : LcSi
}

// --- companion 末端の宣言名 = label が他末端の単純名と衝突（DIA-35） ---

@Enumize
sealed interface Lc2Si

// companion 末端の宣言名（Foo2）が label になる側
class Lc2Host {
    companion object Foo2 : Lc2Si
}

class Lc2Outer {
    object Foo2 : Lc2Si
}

// --- enum 末端の label は enum 全体の単純名（定数名は非関与。DIA-34） ---

@Enumize
sealed interface Lc3Si {
    enum class Dup : Lc3Si { A, B }
}

class Lc3Outer {
    object Dup : Lc3Si
}

// --- 相互非抑止（DIA-34。参照不能末端 Lc4Priv が同居しても双方に発火する） ---

@Enumize
sealed interface Lc4Si

class Lc4A {
    object Same : Lc4Si
}

class Lc4B {
    object Same : Lc4Si
}

// --- 3 末端同名（相手 FQN が列挙される引数形。DIA-34） ---

@Enumize
sealed interface Lc5Si

class Lc5A {
    object Trip : Lc5Si
}

class Lc5B {
    object Trip : Lc5Si
}

class Lc5C {
    object Trip : Lc5Si
}
