import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("org.wrongwrong.sealed-class-enumizer")
}

repositories {
    mavenLocal()
    mavenCentral()
}

// webMain（js / wasmJs 共有の中間ソースセット）は既定の階層テンプレートが作る。
// Windows ホストでも native 不要で HMPP の派生関係（webMain → jsMain）を検証できる構成
kotlin {
    js {
        nodejs()
    }
    // wasm 系のターゲット宣言 DSL は KGP でまだ実験的であり、明示的なオプトインを要求する
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }
}
