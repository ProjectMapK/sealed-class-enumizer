package org.wrongwrong.sealedClassEnumizer.gradle

import org.gradle.api.provider.Property

abstract class SealedClassEnumizerExtension {
    // runtime-api を各コンパイレーションへ自動追加するかどうか（docs/概要.md §7 のオプトアウト）
    abstract val addRuntimeDependency: Property<Boolean>

    init {
        addRuntimeDependency.convention(true)
    }
}
