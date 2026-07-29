package io.github.projectmapk.sweep.taimpl

import kotlin.reflect.KClass

// 明示形の階層内手動実装（末端 class 自身が生成 Enumish を実装する形）。kind は companion 側
data class SwTiExLeaf(val v: Int) : SwTiEx, SwTiEx.Enumish {
    override val label: String get() = "explicit"

    override val enumizedClass: KClass<out SwTiEx> get() = SwTiExLeaf::class
}
