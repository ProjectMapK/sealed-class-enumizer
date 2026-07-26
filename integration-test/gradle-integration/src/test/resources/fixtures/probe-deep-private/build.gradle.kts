plugins {
    kotlin("jvm")
    id("org.wrongwrong.sealed-class-enumizer")
}

repositories {
    mavenLocal()
    mavenCentral()
}

tasks.register<JavaExec>("runMain") {
    classpath = files(sourceSets.main.get().runtimeClasspath)
    mainClass.set("org.wrongwrong.probe.deep.MainKt")
}
