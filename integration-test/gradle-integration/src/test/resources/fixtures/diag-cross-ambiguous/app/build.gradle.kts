plugins {
    kotlin("jvm")
    id("org.wrongwrong.sealed-class-enumizer")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
}
