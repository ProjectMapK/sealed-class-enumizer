// MPP 純消費側（docs/test/テスト戦略.md §4）。プラグイン未適用の KMP モジュールの
// 共通コードから mpp-producer の生成 API を参照できること（跨モジュール × MPP）を検証する
plugins { kotlin("multiplatform") }

group = "org.wrongwrong"

version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)

    jvm()
    js { nodejs() }
    wasmJs { nodejs() }
    wasmWasi { nodejs() }
    val os = System.getProperty("os.name")
    when {
        os.startsWith("Windows") -> mingwX64()
        os.startsWith("Mac") -> macosArm64()
        else -> linuxX64()
    }

    sourceSets {
        commonMain { dependencies { implementation(project(":mpp-producer")) } }
        commonTest { dependencies { implementation(kotlin("test")) } }
    }
}
