package io.github.projectmapk.sealedClassEnumizer.compiler

import io.github.projectmapk.sealedClassEnumizer.Enumish
import io.github.projectmapk.sealedClassEnumizer.EnumishEntriesHolder
import io.github.projectmapk.sealedClassEnumizer.Enumized
import kotlin.reflect.KClass

// EnumishEntriesHolder の protected メンバーは宣言元の外から参照できないため、可視性を広げた
// abstract override の宣言だけを置き、EnumizeNamesTest の参照元とする。
// override 先は runtime-api の宣言そのもの（型引数を Enumish で具体化した形）であるため、
// runtime-api 側の改名・シグネチャ変更はここでコンパイルエラーになる
internal abstract class EnumizeEntriesHolderProbe : EnumishEntriesHolder<Enumish>() {
    public abstract override val enumizedRootClass: KClass<out Enumized<Enumish>>

    public abstract override fun createEntries(): List<Enumish>
}
