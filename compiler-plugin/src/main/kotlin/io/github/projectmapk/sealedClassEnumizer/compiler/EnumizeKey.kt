package io.github.projectmapk.sealedClassEnumizer.compiler

import org.jetbrains.kotlin.GeneratedDeclarationKey

// すべての生成宣言・生成メンバーに origin として付与するキー。IR 側はこのキーで充填対象を発見する
// （docs/コンパイラプラグイン設計01.md §1）
object EnumizeKey : GeneratedDeclarationKey() {
    override fun toString(): String = "SealedClassEnumizer"
}
