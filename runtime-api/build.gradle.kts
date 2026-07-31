import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.maven.publish)
}

kotlin {
    jvmToolchain(17)

    // 公開 jar のバイトコードは JVM 8 互換とする（下限は利用側アプリの実行時 JVM で決まるため広く取る。
    // -Xjdk-release で 9+ の JDK API 参照もコンパイル時に遮断する）。ビルド・テストは toolchain 17 のまま
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            freeCompilerArgs.add("-Xjdk-release=1.8")
        }
    }
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

// Maven Central 公開設定（POM の共通値はルート、モジュール別値は本モジュールの gradle.properties）。
// javadoc jar は空で Central の要件を満たす（Kotlin ライブラリの慣行。Dokka の導入は KDoc 整備とセットで別途判断）
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))
}
