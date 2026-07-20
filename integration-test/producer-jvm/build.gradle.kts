plugins {
    kotlin("jvm")
    id("org.wrongwrong.sealed-class-enumizer")
}

group = "org.wrongwrong"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(kotlin("test"))
    // sealedSubclasses と entries の一致・不一致の観測（TC-ORD-020/021 等）に使用。
    // ランタイム reflection 非依存の原則はプラグイン生成物側の話であり、テストからの観測には利用してよい
    testImplementation(kotlin("reflect"))
}

tasks.test {
    useJUnitPlatform()
}
