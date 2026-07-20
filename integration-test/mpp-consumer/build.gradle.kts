// MPP 純消費側（docs/テストケース管理.md モジュール一覧）。プラグイン未適用の KMP モジュールの
// 共通コードから mpp-producer の生成 API を参照できること（跨モジュール × MPP）を検証する
plugins {
    kotlin("multiplatform")
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
    val os = System.getProperty("os.name")
    when {
        os.startsWith("Windows") -> mingwX64()
        os.startsWith("Mac") -> macosArm64()
        else -> linuxX64()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":mpp-producer"))
                // 暫定: gradle-plugin が runtime-api を implementation で自動追加するため利用側へ伝播しない
                // （docs/修正方針案.md #1。api 化の修正後はこの宣言は不要になる）
                implementation("org.wrongwrong:runtime-api:1.0-SNAPSHOT")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
