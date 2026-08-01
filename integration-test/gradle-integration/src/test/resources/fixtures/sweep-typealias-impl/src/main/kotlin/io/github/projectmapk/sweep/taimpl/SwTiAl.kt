package io.github.projectmapk.sweep.taimpl

import io.github.projectmapk.sealedClassEnumizer.Enumize
import kotlin.reflect.KClass

// 観測対象（別名形）: 階層内の手動実装を typealias 経由で書く基底。
// 別名宣言は SwTiAlias.kt が持つ（生成型への typealias は宣言ファイルの位置で解決順が変わるため分離する）
@Enumize
sealed interface SwTiAl

// 別名形の階層内手動実装（SwTiExLeaf と同一構造で supertype の表記だけが typealias）
data class SwTiAlLeaf(val v: Int) : SwTiAl, SwTiAlias {
    override val label: String get() = "aliased"

    override val enumizedClass: KClass<out SwTiAl> get() = SwTiAlLeaf::class
}

// useSwTiEx と同一形。別名形の手動実装が継承者一覧に載らなければ、この when は網羅と見なされる
fun useSwTiAl(kind: SwTiAl.Enumish): Int = when (kind) {
    SwTiAlLeaf.Companion -> 1
}
