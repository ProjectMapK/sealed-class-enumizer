package io.github.projectmapk.diag.ok

import kotlin.reflect.KClass

// docs/test/ケース04-診断.md DIA-58: 階層内手動実装（末端 class の両実装）は許容・MIOH / MSM 非発火。
// Enumish 由来でも可視な label 手動宣言には ES 警告（docs/test/ケース04-診断.md DIA-37）
class OkManLeaf(val v: Int) : OkManSi, OkManSi.Enumish {
    override val label: String get() = "OkManLeaf"

    override val enumizedClass: KClass<out OkManSi> get() = OkManLeaf::class
}
