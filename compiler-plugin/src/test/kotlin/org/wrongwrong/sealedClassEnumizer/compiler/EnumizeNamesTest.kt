package org.wrongwrong.sealedClassEnumizer.compiler

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import org.wrongwrong.sealedClassEnumizer.Enumish
import org.wrongwrong.sealedClassEnumizer.EnumishCompanion
import org.wrongwrong.sealedClassEnumizer.EnumishEntriesHolder
import org.wrongwrong.sealedClassEnumizer.Enumize
import org.wrongwrong.sealedClassEnumizer.Enumized

// EnumizeNames は runtime-api をロードせず名前をリテラルで持つため、宣言との一致をここで担保する
// （compiler-plugin の単体テストはこの検証のみを持つ。docs/コンパイラプラグイン設計00.md §9）:
// runtime-api 側の宣言を改名すると、参照が追随するこのテストが失敗し、定数の更新漏れを検知できる。
// マップのキーは差分の可読性のためのラベルであり、比較対象は値
class EnumizeNamesTest {
    @Test
    fun `runtime-api 由来の名前定数は宣言と一致する`() {
        assertEquals(namesFromDeclarations(), namesFromConstants())
    }

    private fun namesFromDeclarations(): Map<String, String> =
        mapOf(
            "Enumize" to Enumize::class.java.name,
            "Enumish" to Enumish::class.java.name,
            "EnumishCompanion" to EnumishCompanion::class.java.name,
            "Enumized" to Enumized::class.java.name,
            "EnumishEntriesHolder" to EnumishEntriesHolder::class.java.name,
            "Enumish.label" to Enumish::label.name,
            "Enumish.enumishCompanion" to Enumish::enumishCompanion.name,
            "Enumish.enumizedClass" to Enumish::enumizedClass.name,
            "Enumized.asEnumish" to Enumized<*>::asEnumish.name,
            "EnumishCompanion.entries" to EnumishCompanion<*>::entries.name,
            "EnumishCompanion.valueOf" to EnumishCompanion<*>::valueOf.name,
            "EnumishCompanion.valueOf の値引数" to valueParameterName(EnumishCompanion<*>::valueOf),
            "EnumishCompanion.valueOfOrNull" to EnumishCompanion<*>::valueOfOrNull.name,
            "EnumishEntriesHolder.entries" to EnumishEntriesHolder<*>::entries.name,
            "EnumishEntriesHolder.enumizedRootClass" to
                EnumizeEntriesHolderProbe::enumizedRootClass.name,
            "EnumishEntriesHolder.createEntries" to EnumizeEntriesHolderProbe::createEntries.name,
            "EnumishEntriesHolder.getByLabel" to EnumishEntriesHolder<*>::getByLabel.name,
            "EnumishEntriesHolder.getByLabelOrNull" to
                EnumishEntriesHolder<*>::getByLabelOrNull.name,
        )

    private fun namesFromConstants(): Map<String, String> =
        mapOf(
            "Enumize" to EnumizeNames.ENUMIZE_ANNOTATION_CLASS_ID.asFqNameString(),
            "Enumish" to EnumizeNames.ENUMISH_CLASS_ID.asFqNameString(),
            "EnumishCompanion" to EnumizeNames.ENUMISH_COMPANION_CLASS_ID.asFqNameString(),
            "Enumized" to EnumizeNames.ENUMIZED_CLASS_ID.asFqNameString(),
            "EnumishEntriesHolder" to EnumizeNames.ENTRIES_HOLDER_BASE_CLASS_ID.asFqNameString(),
            "Enumish.label" to EnumizeNames.LABEL.asString(),
            "Enumish.enumishCompanion" to EnumizeNames.ENUMISH_COMPANION_PROPERTY.asString(),
            "Enumish.enumizedClass" to EnumizeNames.ENUMIZED_CLASS_PROPERTY.asString(),
            "Enumized.asEnumish" to EnumizeNames.AS_ENUMISH.asString(),
            "EnumishCompanion.entries" to EnumizeNames.ENTRIES.asString(),
            "EnumishCompanion.valueOf" to EnumizeNames.VALUE_OF.asString(),
            "EnumishCompanion.valueOf の値引数" to EnumizeNames.VALUE_PARAMETER.asString(),
            "EnumishCompanion.valueOfOrNull" to EnumizeNames.VALUE_OF_OR_NULL.asString(),
            // EnumishCompanion と EnumishEntriesHolder の entries は名前が一致しているだけの別宣言だが、
            // 定数は 1 つを共用しているため、どちらが改名されてもここで検知できる
            "EnumishEntriesHolder.entries" to EnumizeNames.ENTRIES.asString(),
            "EnumishEntriesHolder.enumizedRootClass" to EnumizeNames.ENUMIZED_ROOT_CLASS.asString(),
            "EnumishEntriesHolder.createEntries" to EnumizeNames.CREATE_ENTRIES.asString(),
            "EnumishEntriesHolder.getByLabel" to EnumizeNames.GET_BY_LABEL.asString(),
            "EnumishEntriesHolder.getByLabelOrNull" to EnumizeNames.GET_BY_LABEL_OR_NULL.asString(),
        )

    // 値引数名は kotlin-reflect でしか取得できない（KCallable.parameters）。
    // プラグイン本体には持ち込めないため、テストのみで参照する
    private fun valueParameterName(function: KFunction<*>): String =
        checkNotNull(function.parameters.single { it.kind == KParameter.Kind.VALUE }.name)
}
