package io.github.projectmapk.sweep.taimpl

import io.github.projectmapk.sealedClassEnumizer.Enumize
import kotlin.reflect.KClass

// 対照（明示形）: 階層内の手動実装を SwTiEx.Enumish と直接書く基底
@Enumize
sealed interface SwTiEx

// 明示形の階層内手動実装（末端 class 自身が生成 Enumish を実装する形）。kind は companion 側
data class SwTiExLeaf(val v: Int) : SwTiEx, SwTiEx.Enumish {
    override val label: String get() = "explicit"

    override val enumizedClass: KClass<out SwTiEx> get() = SwTiExLeaf::class
}

// 手動実装の枝を欠いた kind-when。手動実装が継承者一覧に載っていれば網羅性エラーになる
fun useSwTiEx(kind: SwTiEx.Enumish): Int = when (kind) {
    SwTiExLeaf.Companion -> 1
}
