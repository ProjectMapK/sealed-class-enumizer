package org.wrongwrong.sealedClassEnumizer.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

// 全コンパイレーション（main / test / metadata / 全ターゲット）へコンパイラプラグインを適用し、
// runtime-api を自動追加する（production は api / test は implementation。docs/概要.md §7・
// docs/コンパイラプラグイン設計00.md §2）
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
        if (!extension.addRuntimeDependency.get()) {
            return
        }
        val coordinates =
            "${SealedClassEnumizerCoordinates.GROUP}:${SealedClassEnumizerCoordinates.RUNTIME_API_ARTIFACT}:${SealedClassEnumizerCoordinates.VERSION}"
        val isTest = kotlinCompilation.isTestCompilation()
        // コンパイレーション単位の dependencies は deprecated のため、既定ソースセットへ宣言する。
        // production（main / metadata）は生成 API の supertype（runtime-api の Enumish / Enumized）を公開する
        // ABI 依存のため、利用側のコンパイルクラスパスへ伝播する api で追加する（概要 §7）。
        // test コンパイレーションは公開面を持たないため implementation で足りる。api を付けると
        // 「Unsupported API dependency types in test source sets」（KT-63142）の警告になる
        kotlinCompilation.defaultSourceSet.dependencies {
            if (isTest) {
                implementation(coordinates)
            } else {
                api(coordinates)
            }
        }
    }

    // test コンパイレーションの名前は "test"（KotlinCompilation.TEST_COMPILATION_NAME）。
    // KT-63142 警告を出す KGP 本体の TestApiDependenciesChecker と同じ判定基準に揃える
    private fun KotlinCompilation<*>.isTestCompilation(): Boolean =
        name == KotlinCompilation.TEST_COMPILATION_NAME
}
