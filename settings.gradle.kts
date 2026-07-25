plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

rootProject.name = "sealed-class-enumizer"

@Suppress("UnstableApiUsage") dependencyResolutionManagement { repositories { mavenCentral() } }

include(":runtime-api", ":compiler-plugin", ":gradle-plugin")
