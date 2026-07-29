package io.github.projectmapk.sealedClassEnumizer

interface Enumized<out T : Enumish> {
    fun asEnumish(): T
}
