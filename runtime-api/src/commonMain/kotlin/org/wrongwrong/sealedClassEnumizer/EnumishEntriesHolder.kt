package org.wrongwrong.sealedClassEnumizer

import kotlin.reflect.KClass

// entries の保持・遅延初期化・label 探索を集約する基底クラス。各階層の EntriesHolder（プラグインが生成）が
// これを継承する。コンストラクタ引数を持たず、基底の識別は enumizedRootClass で行う（docs/概要.md §2）。
// 失敗メッセージが qualifiedName でなく simpleName なのは、qualifiedName が JS 等で利用できないため。
abstract class EnumishEntriesHolder<T : Enumish> {
    protected abstract val enumizedRootClass: KClass<out Enumized<T>>

    protected abstract fun createEntries(): List<T>

    val entries: List<T> by lazy { createEntries() }

    fun getByLabelOrNull(label: String): T? = entries.firstOrNull { it.label == label }

    fun getByLabel(label: String): T =
        getByLabelOrNull(label)
            ?: throw IllegalArgumentException(
                "No enumish entry with label '$label' in ${enumizedRootClass.simpleName}"
            )
}
