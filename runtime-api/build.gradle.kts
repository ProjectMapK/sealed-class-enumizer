import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.dokka)
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
    linuxArm64()
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
    // Android NDK 向けの native ターゲット。Android/JVM（androidTarget）とは別物であり、
    // そちらは jvm variant が受ける（KGP の platform.type 互換規則）
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()
    mingwX64()
}

// Maven Central 公開設定（POM の共通値はルート、モジュール別値は本モジュールの gradle.properties）。
// javadoc jar は空で Central の要件を満たす（API ドキュメントはルートで集約した Dokka が GitHub Pages へ出す）
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))
}

// 公開の取りこぼしの検知。ホストが対応しないターゲットは kotlin.native.ignoreDisabledTargets により
// publication ごと静かに落ち、成果物が欠けたまま公開が成功してしまうため、リモートへの公開時に
// 宣言ターゲットが全て publishable であることを確かめる。
// ローカル公開は対象にしない（ホスト差の吸収は integration-test のフィクスチャ経路の前提であり、
// 検査するのは配布物を作る経路に限る）
val checkPublishableTargets =
    tasks.register("checkPublishableTargets") {
        val unpublishable = kotlin.targets.filterNot { it.publishable }.map { it.name }.sorted()
        doLast {
            if (unpublishable.isNotEmpty()) {
                throw GradleException(
                    "このホストでは公開できないターゲットがあります: ${unpublishable.joinToString()}。" +
                        "宣言した全ターゲットを賄えるホストで公開してください"
                )
            }
        }
    }

tasks.withType<PublishToMavenRepository>().configureEach { dependsOn(checkPublishableTargets) }
