// MPP 生成側（docs/test/テスト戦略.md §4）。commonMain の @Enumize 階層を
// 全ターゲット（jvm / js / native(host) / wasmJs / wasmWasi）で box 検証する。
// native ターゲットはホスト依存でゲートする（docs/test/フィクスチャ構成.md §4）

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("org.wrongwrong.sealed-class-enumizer")
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
    val os = System.getProperty("os.name")
    when {
        os.startsWith("Windows") -> mingwX64()
        os.startsWith("Mac") -> macosArm64()
        else -> linuxX64()
    }

    sourceSets {
        commonTest { dependencies { implementation(kotlin("test")) } }
        // docs/test/ケース05-境界横断.md XMP-40: 中間ソースセット（HMPP）の @Enumize 階層は
        // src/webMain / src/webTest に置く。
        // webMain（js / wasmJs 共有）は既定の階層テンプレートが作る中間ソースセットであり、
        // その metadata コンパイルでも生成が成立すること（V5 の中間ソースセット適用）を検証する。
        // 本来案の nativeMain 共有は単一 native ホスト（Windows = mingwX64 のみ）では
        // metadata コンパイルが立たないため web 共有で代替する
    }
}
