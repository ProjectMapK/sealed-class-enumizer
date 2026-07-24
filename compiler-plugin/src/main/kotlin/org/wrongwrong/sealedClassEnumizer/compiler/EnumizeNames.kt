package org.wrongwrong.sealedClassEnumizer.compiler

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

// runtime-api の宣言（docs/概要.md §2）と生成物（docs/コンパイラプラグイン設計01.md）の名前定数。
// RUNTIME_PACKAGE は runtime-api モジュールのパッケージと一致していなければならない。
object EnumizeNames {
    val RUNTIME_PACKAGE: FqName = FqName("org.wrongwrong.sealedClassEnumizer")

    val ENUMIZE_ANNOTATION_CLASS_ID: ClassId = ClassId(RUNTIME_PACKAGE, Name.identifier("Enumize"))
    val ENUMIZE_ANNOTATION_FQ_NAME: FqName = ENUMIZE_ANNOTATION_CLASS_ID.asSingleFqName()
    val ENUMISH_CLASS_ID: ClassId = ClassId(RUNTIME_PACKAGE, Name.identifier("Enumish"))
    val ENUMISH_COMPANION_CLASS_ID: ClassId = ClassId(RUNTIME_PACKAGE, Name.identifier("EnumishCompanion"))
    val ENUMIZED_CLASS_ID: ClassId = ClassId(RUNTIME_PACKAGE, Name.identifier("Enumized"))
    val ENTRIES_HOLDER_BASE_CLASS_ID: ClassId =
        ClassId(RUNTIME_PACKAGE, Name.identifier("EnumishEntriesHolder"))

    val ENUMISH_NAME: Name = Name.identifier("Enumish")
    val ENTRIES_HOLDER_NAME: Name = Name.identifier("\$EntriesHolder")

    // 参照不能 kind 用の IR-only アクセサ（設計02 §4.3）。末端クラス内にネストする object の名前と、
    // その kind 取得メンバー。file-private な壁に対するトップレベル関数名は kind ごとに一意化して生成する
    val KIND_ACCESSOR_NAME: Name = Name.identifier("\$EnumizeKindAccessor")
    val KIND_ACCESSOR_GET: Name = Name.identifier("get")
    val KIND_ACCESSOR_TOP_LEVEL_PREFIX: String = "\$enumizeKindAccessor\$"

    val LABEL: Name = Name.identifier("label")
    val ENUMISH_COMPANION_PROPERTY: Name = Name.identifier("enumishCompanion")
    val ENUMIZED_CLASS_PROPERTY: Name = Name.identifier("enumizedClass")
    val AS_ENUMISH: Name = Name.identifier("asEnumish")
    val ENTRIES: Name = Name.identifier("entries")
    val VALUE_OF: Name = Name.identifier("valueOf")
    val VALUE_OF_OR_NULL: Name = Name.identifier("valueOfOrNull")
    val ENUMIZED_ROOT_CLASS: Name = Name.identifier("enumizedRootClass")
    val CREATE_ENTRIES: Name = Name.identifier("createEntries")
    val GET_BY_LABEL: Name = Name.identifier("getByLabel")
    val GET_BY_LABEL_OR_NULL: Name = Name.identifier("getByLabelOrNull")
    val VALUE_PARAMETER: Name = Name.identifier("value")
    val TO_STRING: Name = Name.identifier("toString")
}
