plugins {
    kotlin("jvm")
    id("io.github.projectmapk.sealed-class-enumizer")
}

repositories {
    mavenLocal()
    mavenCentral()
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("io.github.projectmapk.probe.deep.MainKt")
}
