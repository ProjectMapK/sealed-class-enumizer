plugins {
    kotlin("jvm")
    id("io.github.projectmapk.sealed-class-enumizer")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
}
