package io.github.projectmapk.sealedClassEnumizer.gradle

import org.gradle.api.provider.Property

abstract class SealedClassEnumizerExtension {
    // runtime-api を各コンパイレーションへ自動追加するかどうか（docs/概要.md §7 のオプトアウト）
    abstract val addRuntimeDependency: Property<Boolean>

    // プロジェクト既定の label ケース。@Enumize が具体の labelCase を指定しない階層へ適用される
    // （docs/概要.md §4）。プロパティ名はコンパイラプラグインの labelCase オプション
    // （= @Enumize の引数名）と同名に揃える。convention はコンパイラプラグインの組み込み既定
    // （EnumizeLabelCase.BUILT_IN_DEFAULT）と同値の AS_DECLARED に固定し、二重既定の乖離を防ぐ
    abstract val labelCase: Property<LabelCase>

    init {
        addRuntimeDependency.convention(true)
        labelCase.convention(LabelCase.AS_DECLARED)
    }
}
