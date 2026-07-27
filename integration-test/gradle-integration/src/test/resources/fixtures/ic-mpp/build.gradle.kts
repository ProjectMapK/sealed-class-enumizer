// MPP の末端追加ラウンド用フィクスチャ。commonMain に多ファイル sealed 階層を置き、
// klib（js / wasmJs）・metadata・jvm の各コンパイルを同一ラウンドで観測する

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("org.wrongwrong.sealed-class-enumizer")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    jvm()
    js {
        nodejs()
    }
    // wasm 系のターゲット宣言 DSL は KGP でまだ実験的であり、明示的なオプトインを要求する
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }
}

// 実行時 entries の観測は jvm ターゲットで行う（klib 側は成果物のバイト比較で観測する）
tasks.register<JavaExec>("runMain") {
    val jvmMain = kotlin.jvm().compilations.getByName("main")
    classpath = files(jvmMain.output.allOutputs, jvmMain.runtimeDependencyFiles)
    mainClass.set("org.wrongwrong.icmpp.MainKt")
}
