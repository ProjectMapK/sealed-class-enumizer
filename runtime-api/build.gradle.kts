import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
}

group = "org.wrongwrong"

version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)

    jvm()
    js { nodejs() }
    // wasm 系のターゲット宣言 DSL は KGP でまだ実験的であり、明示的なオプトインを要求する
    @OptIn(ExperimentalWasmDsl::class) wasmJs { nodejs() }

    @OptIn(ExperimentalWasmDsl::class) wasmWasi { nodejs() }
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()
}
