plugins {
    kotlin("multiplatform")
    id("org.wrongwrong.sealed-class-enumizer")
}

repositories {
    mavenCentral()
}

// webMain（js / wasmJs 共有の中間ソースセット）は既定の階層テンプレートが作る。
// Windows ホストでも native 不要で HMPP の派生関係（webMain → jsMain）を検証できる構成
kotlin {
    js {
        nodejs()
    }
    wasmJs {
        nodejs()
    }
}
