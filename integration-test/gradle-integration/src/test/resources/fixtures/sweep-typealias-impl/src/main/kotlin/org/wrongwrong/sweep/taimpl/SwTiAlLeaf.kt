package org.wrongwrong.sweep.taimpl

import kotlin.reflect.KClass

// 別名形の階層内手動実装（SwTiExLeaf と同一構造で supertype の表記だけが typealias）
data class SwTiAlLeaf(val v: Int) : SwTiAl, SwTiAlias {
    override val label: String get() = "aliased"

    override val enumizedClass: KClass<out SwTiAl> get() = SwTiAlLeaf::class
}
