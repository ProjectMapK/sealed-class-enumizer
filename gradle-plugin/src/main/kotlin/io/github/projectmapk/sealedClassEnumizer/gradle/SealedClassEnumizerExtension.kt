package io.github.projectmapk.sealedClassEnumizer.gradle

import org.gradle.api.provider.Property

abstract class SealedClassEnumizerExtension {
    // runtime-api を各コンパイレーションへ自動追加するかどうか（docs/概要.md §7 のオプトアウト）
    abstract val addRuntimeDependency: Property<Boolean>

    // プロジェクト既定の label ケース。@Enumize が具体の labelCase を指定しない階層へ適用される
    // （docs/概要.md §4）。convention はコンパイラプラグインの組み込み既定と同値の AS_DECLARED
    abstract val labelCase: Property<LabelCase>

    init {
        addRuntimeDependency.convention(true)
        labelCase.convention(LabelCase.AS_DECLARED)
    }
}
