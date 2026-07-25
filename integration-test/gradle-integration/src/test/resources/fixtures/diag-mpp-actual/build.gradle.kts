plugins {
    kotlin("multiplatform")
    id("org.wrongwrong.sealed-class-enumizer")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm()
}
