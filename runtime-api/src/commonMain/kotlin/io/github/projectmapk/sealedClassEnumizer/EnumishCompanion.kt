package io.github.projectmapk.sealedClassEnumizer

// Enumish 階層の操作系 API（entries / valueOf）の集約点。生成 Enumish の companion object が
// 具体型 T へ絞り込んで実装する。out T の宣言側共変により、複数階層の companion object を
// List<EnumishCompanion<Enumish>> として射影なしで束ねられる（docs/概要.md §2）
interface EnumishCompanion<out T : Enumish> {
    val entries: List<T>

    fun valueOf(value: String): T

    fun valueOfOrNull(value: String): T?
}
