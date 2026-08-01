package io.github.projectmapk.sealedClassEnumizer.maven

import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import org.codehaus.plexus.logging.Logger
import org.jetbrains.kotlin.maven.KotlinMavenPluginExtension
import org.w3c.dom.Element

// Plexus コンポーネント登録（META-INF/plexus/components.xml）と実装の対応を固定する。
// 登録は実行時にしか効かず、名前がずれても本モジュールのコンパイルは通るため、
// role / role-hint / implementation / requirement のフィールドをここで実体と突き合わせる
class PlexusComponentsTest {
    private data class Registration(
        val role: String,
        val roleHint: String,
        val implementation: String,
        val requirementRole: String,
        val requirementField: String,
        // requirement の注入先が実装に在ることの確認（型は宣言された role と一致していなければならない）
        val injectedFieldType: String,
        val implementsExtensionPoint: Boolean,
    )

    @Test
    fun componentDescriptorMatchesImplementation() {
        val component = readSingleComponent()
        val requirement =
            singleChildElement(component, "requirements").let {
                singleChildElement(it, "requirement")
            }
        val implementationClass = Class.forName(text(component, "implementation"))
        val injectedField = implementationClass.getDeclaredField(text(requirement, "field-name"))
        assertEquals(
            Registration(
                role = KotlinMavenPluginExtension::class.java.name,
                roleHint = SealedClassEnumizerMavenPluginExtension.PLUGIN_NAME,
                implementation = SealedClassEnumizerMavenPluginExtension::class.java.name,
                requirementRole = Logger::class.java.name,
                requirementField = "logger",
                injectedFieldType = Logger::class.java.name,
                implementsExtensionPoint = true,
            ),
            Registration(
                role = text(component, "role"),
                roleHint = text(component, "role-hint"),
                implementation = implementationClass.name,
                requirementRole = text(requirement, "role"),
                requirementField = injectedField.name,
                injectedFieldType = injectedField.type.name,
                implementsExtensionPoint =
                    KotlinMavenPluginExtension::class.java.isAssignableFrom(implementationClass),
            ),
        )
    }

    private fun readSingleComponent(): Element {
        val resource =
            requireNotNull(javaClass.classLoader.getResource(COMPONENTS_RESOURCE)) {
                "$COMPONENTS_RESOURCE がクラスパスに無い"
            }
        val document =
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resource.toString())
        return singleChildElement(document.documentElement, "components").let {
            singleChildElement(it, "component")
        }
    }

    private fun singleChildElement(parent: Element, name: String): Element {
        val children = childElements(parent, name)
        assertEquals(1, children.size, "$name の要素数")
        return children.single()
    }

    private fun childElements(parent: Element, name: String): List<Element> =
        (0 until parent.childNodes.length)
            .map(parent.childNodes::item)
            .filterIsInstance<Element>()
            .filter { it.tagName == name }

    private fun text(parent: Element, name: String): String =
        singleChildElement(parent, name).textContent.trim()

    private companion object {
        const val COMPONENTS_RESOURCE: String = "META-INF/plexus/components.xml"
    }
}
