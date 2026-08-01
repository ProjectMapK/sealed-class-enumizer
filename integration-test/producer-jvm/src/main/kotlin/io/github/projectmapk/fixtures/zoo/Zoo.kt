package io.github.projectmapk.fixtures.zoo

import io.github.projectmapk.sealedClassEnumizer.Enumize

// 全種別末端 12 の合成階層（docs/test/ケース01-生成と実行時API.md API-12〜API-18・
// docs/test/ケース03-順序.md ORD-04）。
// object 系は自身が kind、class / interface / enum / value class 系は companion が kind。
// IfaceLeaf / FunLeaf は明示 public companion、それ以外は自動生成 companion。
// 吸収サブタイプは本ファイル末尾のトップレベル群とテスト内（無名 object・local class）に分散する
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

// --- 吸収サブタイプ（docs/test/ケース01-生成と実行時API.md API-13/API-14）。
//     いずれも自 kind を作らず、最上位末端の kind へ吸収される ---

// open 末端の直下サブタイプ
class Oval : Zoo.OpenLeaf()

// 多段吸収の中継サブタイプ（末端は Square）
open class Quad : Zoo.OpenLeaf()

// 多段サブタイプ（OpenLeaf ← Quad ← Square の 2 段でも最上位末端 OpenLeaf の kind へ吸収される。API-14）
class Square : Quad()

// object であっても自 kind を作らない
object Spot : Zoo.OpenLeaf()

// interface 末端の第三者実装（default 実装の asEnumish を JVM lowering 込みで継承する）
class Crafted : Zoo.IfaceLeaf

// 末端 interface への委譲実装（委譲された asEnumish が IfaceLeaf の kind を返す）。
// 基底 Zoo への直接委譲は診断対象（docs/test/ケース04-診断.md DIA-69）であり、ここでは末端への委譲のみ扱う
class Veil(impl: Zoo.IfaceLeaf) : Zoo.IfaceLeaf by impl

// 非 sealed 末端の配下に置いた sealed 部分階層。階層の探索は非 sealed 末端で止まるため、
// 中間 Brood も末端 Chick も自 kind を作らず IfaceLeaf の kind へ吸収される
sealed interface Brood : Zoo.IfaceLeaf

// sealed 部分階層の末端（上向きの候補判定は Brood を辿るが IfaceLeaf で止まる）
data object Chick : Brood
