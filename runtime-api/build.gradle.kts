plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "org.wrongwrong"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)

    jvm()
    js {
        nodejs()
    }
    wasmJs {
        nodejs()
    }
    wasmWasi {
        nodejs()
    }
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()
}
