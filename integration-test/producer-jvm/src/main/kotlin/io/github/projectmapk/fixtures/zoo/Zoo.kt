package io.github.projectmapk.fixtures.zoo

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 全種別末端 12 の合成階層（docs/test/ケース01-生成と実行時API.md API-12〜API-18・
// docs/test/ケース03-順序.md ORD-04）。
// object 系は自身が kind、class / interface / enum / value class 系は companion が kind。
// IfaceLeaf / FunLeaf は明示 public companion、それ以外は自動生成 companion。
// 吸収サブタイプは Oval.kt / Quad.kt / Square.kt / Spot.kt / Crafted.kt / Veil.kt と
// テスト内（無名 object・local class）に分散する
@Enumize
sealed interface Zoo {
    data class DataLeaf(val v: Int) : Zoo

    data object ObjectLeaf : Zoo

    object PlainObject : Zoo

    class FinalLeaf(val v: Int) : Zoo

    open class OpenLeaf : Zoo

    abstract class AbstractLeaf : Zoo

    // 明示 public companion 付き interface 末端（kind = 明示 companion・default asEnumish がそれを返す）
    interface IfaceLeaf : Zoo {
        companion object
    }

    // 明示 public companion 付き fun interface 末端（SAM の抽象は go のみに保たれる）
    fun interface FunLeaf : Zoo {
        fun go(): Int

        companion object
    }

    // companion 自動生成側の fun interface 末端（自動生成 × SAM 保持の分岐）
    fun interface FunAuto : Zoo {
        fun run(x: Int): Int
    }

    // 実装者ゼロの interface 末端（自動生成 companion。API-15）
    interface Ghost : Zoo

    enum class EnumLeaf : Zoo {
        ONE
    }

    @JvmInline value class ValueLeaf(val x: Int) : Zoo
}
