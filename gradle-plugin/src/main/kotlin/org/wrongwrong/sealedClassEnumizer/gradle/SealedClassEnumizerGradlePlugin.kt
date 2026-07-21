package org.wrongwrong.sealedClassEnumizer.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

// 全コンパイレーション（main / test / metadata / 全ターゲット）へコンパイラプラグインを適用し、
// runtime-api を自動追加する（docs/概要.md §7・docs/コンパイラプラグイン設計00.md §2）
class SealedClassEnumizerGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.extensions.create("sealedClassEnumizer", SealedClassEnumizerExtension::class.java)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = SealedClassEnumizerCoordinates.COMPILER_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = SealedClassEnumizerCoordinates.GROUP,
        artifactId = SealedClassEnumizerCoordinates.COMPILER_PLUGIN_ARTIFACT,
        version = SealedClassEnumizerCoordinates.VERSION,
    )

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        addRuntimeDependencyIfEnabled(project, kotlinCompilation)
        return project.provider { emptyList() }
    }

    private fun addRuntimeDependencyIfEnabled(project: Project, kotlinCompilation: KotlinCompilation<*>) {
        val extension = project.extensions.getByType(SealedClassEnumizerExtension::class.java)
        if (extension.addRuntimeDependency.get()) {
            // コンパイレーション単位の dependencies は deprecated のため、既定ソースセットへ宣言する。
            // 生成 API は runtime-api の型（Enumish / Enumized）を supertype として公開する ABI 依存のため、
            // 利用側のコンパイルクラスパスへ伝播する api スコープで追加する（概要 §7）
            kotlinCompilation.defaultSourceSet.dependencies {
                api(
                    "${SealedClassEnumizerCoordinates.GROUP}:${SealedClassEnumizerCoordinates.RUNTIME_API_ARTIFACT}:${SealedClassEnumizerCoordinates.VERSION}"
                )
            }
        }
    }
}
