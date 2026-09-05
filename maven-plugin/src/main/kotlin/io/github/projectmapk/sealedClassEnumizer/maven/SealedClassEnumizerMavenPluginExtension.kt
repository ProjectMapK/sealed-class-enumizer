package io.github.projectmapk.sealedClassEnumizer.maven

import org.apache.maven.plugin.MojoExecution
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.logging.Logger
import org.jetbrains.kotlin.maven.KotlinMavenPluginExtension
import org.jetbrains.kotlin.maven.PluginOption

/**
 * Applies the sealed-class-enumizer compiler plugin to the kotlin-maven-plugin compilations that
 * list it, and passes the project-wide default label case to it.
 *
 * Declare the artifact as a dependency of kotlin-maven-plugin and name the plugin in
 * `compilerPlugins`; the compiler plugin itself comes along as a transitive dependency:
 * ```xml
 * <plugin>
 *     <groupId>org.jetbrains.kotlin</groupId>
 *     <artifactId>kotlin-maven-plugin</artifactId>
 *     <configuration>
 *         <compilerPlugins>
 *             <plugin>sealed-class-enumizer</plugin>
 *         </compilerPlugins>
 *     </configuration>
 *     <dependencies>
 *         <dependency>
 *             <groupId>io.github.projectmapk</groupId>
 *             <artifactId>sealed-class-enumizer-maven-plugin</artifactId>
 *             <version>${sealed-class-enumizer.version}</version>
 *         </dependency>
 *     </dependencies>
 * </plugin>
 * ```
 *
 * The configuration above applies to every execution of kotlin-maven-plugin, production and test
 * compilations alike. The runtime API is not added automatically: declare
 * `io.github.projectmapk:sealed-class-enumizer-runtime-api-jvm` in the project `dependencies`,
 * since the generated API exposes its types as supertypes.
 *
 * The project-wide default label case is read from the `sealed-class-enumizer.labelCase` property
 * and defaults to [LabelCase.AS_DECLARED] (no conversion). It applies to hierarchies whose
 * `@Enumize` does not specify a concrete `labelCase`:
 * ```xml
 * <properties>
 *     <sealed-class-enumizer.labelCase>UPPER_SNAKE_CASE</sealed-class-enumizer.labelCase>
 * </properties>
 * ```
 *
 * The same option can also be given through the kotlin-maven-plugin `pluginOptions`
 * (`sealed-class-enumizer:labelCase=UPPER_SNAKE_CASE`). Such a declaration takes precedence: the
 * property is then not passed at all, since the compiler option accepts a single value.
 */
class SealedClassEnumizerMavenPluginExtension : KotlinMavenPluginExtension {
    // Plexus の requirement 注入（META-INF/plexus/components.xml）で設定される。
    // 用途は対応外 Kotlin の警告のみで、オプションの組み立てには関与しない
    lateinit var logger: Logger

    override fun getCompilerPluginId(): String = SealedClassEnumizerCoordinates.COMPILER_PLUGIN_ID

    // goal による絞り込みは行わない（compile / test-compile の双方へ適用する）。
    // 適用範囲は利用側が <compilerPlugins> の記載位置で選ぶ
    override fun isApplicable(project: MavenProject, execution: MojoExecution): Boolean = true

    override fun getPluginOptions(
        project: MavenProject,
        execution: MojoExecution,
    ): List<PluginOption> {
        warnIfUnsupportedKotlin(project, execution)
        // 実行単位の直接指定がある場合はプロジェクト既定を渡さない。オプションは複数指定を
        // 受け付けず（コンパイラ側が「Multiple values are not allowed」で失敗する）、
        // 直接指定は mojo 自身が渡すため、こちらが降りることで優先関係が成立する
        if (declaresLabelCaseOption(execution)) {
            return emptyList()
        }
        // プロジェクト既定の label ケースを常に具体値で渡す（docs/概要.md §4）。
        // キーはコンパイラプラグインの EnumizeCommandLineProcessor.LABEL_CASE_OPTION_NAME と
        // 一致していなければならない
        return listOf(
            PluginOption(
                PLUGIN_NAME,
                getCompilerPluginId(),
                LABEL_CASE_OPTION_NAME,
                resolveLabelCase(project).name,
            )
        )
    }

    // kotlin-maven-plugin の <pluginOptions> に本プラグインの labelCase 指定が在るか
    // （表記は mojo の解釈と同じ <プラグイン名>:<キー>=<値>）
    private fun declaresLabelCaseOption(execution: MojoExecution): Boolean {
        val declared = execution.configuration?.getChild(PLUGIN_OPTIONS_ELEMENT) ?: return false
        return declared.children.any {
            it.value?.trim()?.startsWith("$PLUGIN_NAME:$LABEL_CASE_OPTION_NAME=") == true
        }
    }

    // プロジェクト既定の label ケース。未指定時の既定はコンパイラプラグインの組み込み既定
    // （EnumizeLabelCase.BUILT_IN_DEFAULT）と同値の AS_DECLARED に固定し、二重既定の乖離を防ぐ。
    // 不正値は診断が読みにくいコンパイラ側の失敗になる前に、設定面の名前を示して弾く
    private fun resolveLabelCase(project: MavenProject): LabelCase {
        val declared = project.properties.getProperty(LABEL_CASE_PROPERTY)?.trim().orEmpty()
        if (declared.isEmpty()) {
            return LabelCase.AS_DECLARED
        }
        return LabelCase.entries.firstOrNull { it.name == declared }
            ?: throw IllegalArgumentException(
                "Unknown value for the '$LABEL_CASE_PROPERTY' property: '$declared'. " +
                    "Expected one of: ${LabelCase.entries.joinToString { it.name }}"
            )
    }

    // 対応外の Kotlin への適用は警告のみとする（サポートは KOTLIN_VERSION と同一マイナー。
    // 非互換の実害はコンパイル時に顕在化するため、原因を示す警告に留めてエラーにはしない）
    private fun warnIfUnsupportedKotlin(project: MavenProject, execution: MojoExecution) {
        // 適用先の Kotlin 版は、本拡張を読み込んでいる kotlin-maven-plugin 自身の版である
        val appliedKotlinVersion = execution.plugin?.version ?: return
        if (
            KotlinVersionSupport.isSupported(
                appliedKotlinVersion,
                SealedClassEnumizerCoordinates.KOTLIN_VERSION,
            )
        ) {
            return
        }
        logger.warn(
            "sealed-class-enumizer ${SealedClassEnumizerCoordinates.VERSION} targets Kotlin " +
                "${SealedClassEnumizerCoordinates.KOTLIN_VERSION} and is not verified against " +
                "Kotlin $appliedKotlinVersion applied to ${project.artifactId}. Compilation may " +
                "fail; use the plugin version matching your Kotlin version."
        )
    }

    companion object {
        // 利用側が <compilerPlugins> へ書く名前であり、Plexus の role-hint（components.xml）と
        // 一致していなければならない（PlexusComponentsTest がガードする）
        const val PLUGIN_NAME: String = "sealed-class-enumizer"

        /**
         * Name of the property holding the project-wide default label case. See
         * [SealedClassEnumizerMavenPluginExtension].
         */
        const val LABEL_CASE_PROPERTY: String = "$PLUGIN_NAME.labelCase"

        private const val LABEL_CASE_OPTION_NAME: String = "labelCase"

        // kotlin-maven-plugin の設定要素名（利用者が直接指定を書く場所）
        private const val PLUGIN_OPTIONS_ELEMENT: String = "pluginOptions"
    }
}
