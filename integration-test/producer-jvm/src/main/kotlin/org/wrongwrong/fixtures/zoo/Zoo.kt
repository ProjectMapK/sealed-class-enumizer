package org.wrongwrong.fixtures.zoo

import org.wrongwrong.sealedClassEnumizer.Enumize

// 全種別混在階層（TC-LEAF-011）: 末端 1 種類につき kind が 1 つずつ entries に載ることを観測する。
// object 系は自身が kind、class / interface / enum / value class 系は companion が kind
@Enumize
sealed interface Zoo {
    data class DataLeaf(val v: Int) : Zoo

    data object ObjectLeaf : Zoo

    object PlainObject : Zoo

    open class OpenLeaf : Zoo

    abstract class AbstractLeaf : Zoo

    interface IfaceLeaf : Zoo

    fun interface FunLeaf : Zoo {
        fun go(): Int
    }

    enum class EnumLeaf : Zoo { ONE }

    @JvmInline
    value class ValueLeaf(val x: Int) : Zoo
}
