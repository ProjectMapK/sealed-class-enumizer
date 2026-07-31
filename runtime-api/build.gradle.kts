import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
}

group = "io.github.projectmapk"

version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)

    jvm()
    js { nodejs() }
    // wasm 系のターゲット宣言 DSL は KGP でまだ実験的であり、明示的なオプトインを要求する
    @OptIn(ExperimentalWasmDsl::class) wasmJs { nodejs() }

    @OptIn(ExperimentalWasmDsl::class) wasmWasi { nodejs() }
    linuxX64()
    // macosX64 は Intel Mac 廃止に伴い KGP 2.4 で非推奨化されたが、代替ターゲットは無く
    // （macosArm64 は別アーキテクチャ）、Intel Mac 向け klib の公開を維持するため宣言を残す。
    // KGP から関数が削除された時点で改めて対応する
    @Suppress("DEPRECATION") macosX64()

    macosArm64()
    // iOS 系。純 Kotlin の klib のため宣言は mac 以外のホストでも成立し、
    // ホストで実行不能な分の扱いは gradle.properties の kotlin.native.ignoreDisabledTargets が受ける
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    mingwX64()
}
