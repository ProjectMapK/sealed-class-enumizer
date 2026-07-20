package org.wrongwrong.sealedClassEnumizer

interface Enumized<out T : Enumish> {
    fun asEnumish(): T
}
