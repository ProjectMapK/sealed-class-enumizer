plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
    // gradle-plugin が implementation で自動追加する runtime-api は利用側へ伝播しない（docs/修正方針案.md #1）
    implementation("org.wrongwrong:runtime-api:1.0-SNAPSHOT")
}
