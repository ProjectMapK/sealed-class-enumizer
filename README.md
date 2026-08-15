# sealed-class-enumizer

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.projectmapk.sealed-class-enumizer)](https://plugins.gradle.org/plugin/io.github.projectmapk.sealed-class-enumizer)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.projectmapk/sealed-class-enumizer-runtime-api)](https://central.sonatype.com/artifact/io.github.projectmapk/sealed-class-enumizer-runtime-api)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

A Kotlin (K2) compiler plugin that generates enum-like operations — `entries`, `valueOf`, `label`
and friends — for `sealed class` / `sealed interface` hierarchies at compile time.  
Hierarchies keep their expressive power — data-carrying entries, exhaustive `when` with smart
casts, open leaves — and gain the operational API of enums on top.  
No runtime reflection involved, working on all Kotlin Multiplatform targets.

## Setup

### Gradle

Published on the
[Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.github.projectmapk.sealed-class-enumizer),
so `plugins {}` resolves it without extra repository configuration:

```kotlin
plugins {
    kotlin("jvm") version "2.4.10" // or kotlin("multiplatform") — any Kotlin target plugin
    id("io.github.projectmapk.sealed-class-enumizer") version "2.4.10-0.1.0"
}
```

The Gradle plugin applies the compiler plugin to every compilation and automatically adds the
runtime API (`io.github.projectmapk:sealed-class-enumizer-runtime-api`) — with `api` scope for
production code, because the generated API exposes runtime API types as supertypes.

```kotlin
sealedClassEnumizer {
    addRuntimeDependency = true       // set false to declare the runtime API manually
    labelCase = LabelCase.AS_DECLARED // project-wide default label case (see Label customization)
}
```

<details>
<summary><strong>Maven</strong></summary>

Declare the plugin as a dependency of kotlin-maven-plugin and name it in `compilerPlugins`; the
compiler plugin follows as a transitive dependency. It applies to every kotlin-maven-plugin
execution, production and test compilations alike.

```xml
<plugin>
  <groupId>org.jetbrains.kotlin</groupId>
  <artifactId>kotlin-maven-plugin</artifactId>
  <version>${kotlin.version}</version>
  <configuration>
    <compilerPlugins>
      <plugin>sealed-class-enumizer</plugin>
    </compilerPlugins>
  </configuration>
  <dependencies>
    <dependency>
      <groupId>io.github.projectmapk</groupId>
      <artifactId>sealed-class-enumizer-maven-plugin</artifactId>
      <version>2.4.10-0.1.0</version>
    </dependency>
  </dependencies>
</plugin>
```

Unlike the Gradle plugin, the runtime API is not added for you — declare it as a project
dependency (Maven resolves the platform artifact, hence the `-jvm` suffix):

```xml
<dependency>
  <groupId>io.github.projectmapk</groupId>
  <artifactId>sealed-class-enumizer-runtime-api-jvm</artifactId>
  <version>2.4.10-0.1.0</version>
</dependency>
```

The project-wide default label case is a property:

```xml
<properties>
  <sealed-class-enumizer.labelCase>AS_DECLARED</sealed-class-enumizer.labelCase>
</properties>
```

The same option can be given through kotlin-maven-plugin's `pluginOptions`
(`sealed-class-enumizer:labelCase=...`), which takes precedence over the property.

</details>

### IntelliJ IDEA

IntelliJ's K2 mode does not load third-party compiler plugins by default, so generated
declarations show as unresolved in the editor even though Gradle builds succeed
([KTIJ-29248](https://youtrack.jetbrains.com/issue/KTIJ-29248)).  
To make resolution and completion work, disable the registry flag
`kotlin.k2.only.bundled.compiler.plugins.enabled` (Help | Find Action… | "Registry…"), then
re-sync the project:

![The Registry dialog with `kotlin.k2.only.bundled.compiler.plugins.enabled` unchecked](assets/intellij-registry-flag.png)

This IDE capability is experimental; command-line and Gradle builds are unaffected either way.

Once the flag is disabled, IntelliJ resolves the generated declarations and renders them inline:

![Generated declarations of an `@Enumize` sealed interface rendered inline in IntelliJ IDEA](assets/intellij-generated-declarations.png)

## Usage

```kotlin
import io.github.projectmapk.sealedClassEnumizer.Enumize
import io.github.projectmapk.sealedClassEnumizer.label

@Enumize
sealed interface SI {
    data class Foo(val v: Int) : SI
    data object Bar : SI
}

// enum-like operations; one singleton ("kind") per leaf
SI.Enumish.entries            // [Bar, Foo] — compiler-provided order
SI.Enumish.valueOf("Foo")     // label-based lookup; IllegalArgumentException when absent
SI.Enumish.valueOfOrNull("X") // null-returning variant (an addition over enums)
SI.Enumish.entries.map { it.enumizedClass } // [Bar::class, Foo::class] — all leaf classes

// from a value to its kind
val si: SI = SI.Foo(42)
si.asEnumish()                // Foo's kind (= Foo.Companion)
si.label                      // "Foo" — enum's `name` counterpart
si.asEnumish().enumizedClass  // SI.Foo::class — the bridge to reflection-based libraries

// exhaustive when over kinds (the generated Enumish is sealed — no else branch needed)
when (si.asEnumish()) {
    SI.Foo -> println("a Foo")
    SI.Bar -> println("a Bar")
}
```

Notes:

- `entries` order is the compiler-provided inheritor order (FQN-based), not declaration order.  
  Do not persist positions in the list; persist `label` or a custom property instead.
- `entries.map { it.enumizedClass }` lists every leaf class without reflection —
  `KClass.sealedSubclasses` needs the JVM and `kotlin-reflect`, and silently breaks under R8
  ([KT-25871](https://youtrack.jetbrains.com/issue/KT-25871),
  [KT-37292](https://youtrack.jetbrains.com/issue/KT-37292)).
- `ordinal` and `Comparable` are deliberately not provided: such numbers change on renames and
  must not be persisted.
- Leaves may stay open (`open` / `abstract` class, `interface`, `fun interface`): subtypes defined
  outside the hierarchy are absorbed into the leaf's kind, so `entries` stays fixed while
  implementations remain extensible.
- Members always win over extensions in Kotlin: if the hierarchy declares its own `label` member,
  the extension is shadowed (the plugin warns about it) — `asEnumish().label` is always reliable.

### Kinds as parameters

A leaf that carries data has no instance until the data exists, so on a plain sealed hierarchy
"which status" cannot appear in a signature.  
The usual escape hatches — fabricating a throwaway `Status` from dummy data, or defining a
parallel enum that must be kept in sync — are exactly what kinds eliminate: they are
always-available singletons, accepted wherever an enum would have been:

```kotlin
@Enumize
sealed interface Status {
    data class Active(val remarks: String) : Status
    data class Suspended(val remarks: String) : Status
    data object Deleted : Status
}

class Foo(val status: Status)

interface FooRepository {
    fun searchFoo(vararg statuses: Status.Enumish): List<Foo>
}

class InMemoryFooRepository(private val foos: List<Foo>) : FooRepository {
    override fun searchFoo(vararg statuses: Status.Enumish): List<Foo> =
        foos.filter { it.status.asEnumish() in statuses }
}

// call sites read like enums — no Status instance is fabricated or fetched;
// a data class's kind (Active) and a data object (Deleted) pass uniformly
repository.searchFoo(Status.Active, Status.Deleted)
```

This automates the hand-written workaround of giving every leaf a companion that implements a
shared marker interface
([background article, Japanese](https://qiita.com/wrongwrong/items/e32179fb851a721007a6)).

### Listing every case

Data-carrying leaves have no instances to enumerate, so "all statuses" for a picker or a report
axis traditionally means a hand-maintained list — one that silently goes stale when a leaf is
added.  
`entries` is compiler-generated and complete by construction:

```kotlin
// a filter UI offering every status — new leaves show up without touching this code
val statusOptions: List<String> = Status.Enumish.entries.map { it.label }

// aggregation axes that keep empty groups (groupBy alone would drop them)
val fooCountByStatus: Map<Status.Enumish, Int> =
    Status.Enumish.entries.associateWith { 0 } +
        foos.groupingBy { it.status.asEnumish() }.eachCount()
```

### Round-tripping labels

Persisting "which case" — a DB column, a query parameter, an analytics event — normally takes a
hand-written string mapping that must follow the hierarchy.  
`label` / `valueOf` are that mapping, generated; unlike `value::class.simpleName`, labels are
compile-time constants — never null and unaffected by R8 / minification renaming:

```kotlin
// outbound: labels are the wire form
fun statusQuery(selection: Set<Status.Enumish>): String =
    selection.joinToString("&") { "status=${it.label}" }

// inbound: GET /foos?status=Active&status=Deleted — parsed kinds feed searchFoo from above
fun handle(rawStatuses: List<String>): List<Foo> {
    val statuses = rawStatuses.map {
        requireNotNull(Status.Enumish.valueOfOrNull(it)) { "unknown status: $it" }
    }
    return repository.searchFoo(*statuses.toTypedArray())
}
```

`@EnumishLabel` keeps persisted labels stable across leaf renames — see
[Label customization](#label-customization).

### Verifying per-kind wiring

Not all per-kind wiring fits an exhaustive `when` — handler registries assembled by DI or icon
sets contributed by feature modules live in data, where the compiler cannot check completeness.  
`entries` turns "one per leaf" into a single assertion:

```kotlin
// the map is assembled elsewhere — no single `when` site exists
class StatusRenderer(private val cells: Map<Status.Enumish, CellRenderer>) {
    init {
        val missing = Status.Enumish.entries - cells.keys
        require(missing.isEmpty()) { "statuses without a renderer: ${missing.map { it.label }}" }
    }
}
```

### Exhaustive tests over every leaf

JUnit's `@EnumSource` has no sealed counterpart, and community substitutes build on
`sealedSubclasses` — JVM-only reflection again.  
`entries` drives a test over every leaf on any target, and `enumizedClass` states the expected
type of a value obtained through a kind:

```kotlin
// production code: a per-kind factory (form defaults, DB seeding, fixtures, …)
fun defaultStatusOf(kind: Status.Enumish): Status =
    when (kind) {
        Status.Active -> Status.Active(remarks = "")
        Status.Suspended -> Status.Suspended(remarks = "payment failed")
        Status.Deleted -> Status.Deleted
    }

class DefaultStatusTest {
    @Test
    fun `every status yields a default of its own type`() {
        for (kind in Status.Enumish.entries) {
            assertEquals(kind.enumizedClass, defaultStatusOf(kind)::class)
        }
    }
}
```

A new leaf fails the factory's `when` at compile time; a branch fabricating the wrong case fails
the `enumizedClass` assertion.  
Values of an open leaf's absorbed subtypes report their runtime class via `::class` — compare
kinds instead (`assertEquals(kind, value.asEnumish())`) in such hierarchies.

## Generated API

Conceptually the plugin generates the following (in compiler internals — no source files are
produced):

```kotlin
sealed interface SI : Enumized<SI.Enumish> {
    sealed interface Enumish : io.github.projectmapk.sealedClassEnumizer.Enumish {
        companion object : EnumishCompanion<Enumish> {
            val entries: List<Enumish>
            fun valueOf(value: String): Enumish
            fun valueOfOrNull(value: String): Enumish?
        }
    }
    // and per leaf: asEnumish(), plus the kind singleton —
    // the leaf object itself, or the leaf class's companion object
}
```

Modules that merely consume a library built with this plugin do not need the plugin: the generated
API is regular metadata, resolvable from any module, including exhaustive `when` checks.

## Label customization

Labels default to the leaf's simple name and resolve with this priority:

1. `@EnumishLabel("...")` on the leaf — used as-is (no case conversion); for resolving label
   clashes and keeping persisted labels stable across renames
2. `@Enumize(labelCase = ...)` on the hierarchy
3. The project-wide default from the build — the Gradle DSL `sealedClassEnumizer { labelCase }`
   or the Maven `sealed-class-enumizer.labelCase` property
4. `AS_DECLARED` (no conversion)

Available cases: `AS_DECLARED`, `UPPER_SNAKE_CASE`, `SNAKE_CASE`, `KEBAB_CASE`.  
Word splitting matches kotlinx.serialization's `JsonNamingStrategy` (`HTTPServer` →
`HTTP_SERVER`), is locale-independent, and conversion results are frozen across releases because
labels are meant to be persisted.  
Label uniqueness within a hierarchy is enforced at compile time.

## Compatibility

| | |
|---|---|
| Kotlin | 2.4.x, K2 only. The plugin version encodes the Kotlin minor it targets; applying it to another Kotlin minor produces a build warning |
| Version format | `<KotlinVersion>-<pluginVersion>`, e.g. `2.4.10-0.1.0` |
| Build environment | Gradle 9+ or Maven 3.9+ / JDK 17+ |
| runtime-api (JVM) | Java 8+ bytecode |
| runtime-api targets | `jvm`, `js`, `wasmJs`, `wasmWasi`, `linuxX64`, `mingwX64`, `macosX64`, `macosArm64`, `iosArm64`, `iosSimulatorArm64`, `iosX64` |

## Known limitations

- IntelliJ K2 mode shows generated declarations as unresolved by default — see
  [Setup](#intellij-idea) for the registry workaround.
- A `typealias` that points to a generated `Enumish` cannot be used as the first supertype of the
  `@Enumize` base (a language-side constraint; usage as a type argument or on leaves is fine).
- `@Enumize` cannot be applied to `expect` / `actual` declarations.
- `ordinal` / `Comparable` are not provided by design (see Usage notes).

## Versioning and support policy

- Versions follow `<KotlinVersion>-<pluginVersion>`.  
  All published artifacts (runtime API, compiler plugin, Gradle plugin and its marker, Maven
  plugin) share the same version.
- The Kotlin compiler plugin API has no stability guarantee, so each release targets exactly one
  Kotlin minor; support covers the latest stable Kotlin minor including its patch releases.
- New Kotlin releases are followed by new plugin releases; older minors receive no backports.
- Label case conversion results never change across releases.

## License

Apache License 2.0 — see [LICENSE](LICENSE).  
Design documents (Japanese) live under [docs/](docs/).
