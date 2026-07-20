// diag-cross-absorb フィクスチャ（docs/テストケース管理.md「Gradle TestKit 方針」・G 軸 診断カタログ）
pluginManagement {
    includeBuild("%%PARENT_BUILD%%")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
        kotlin("multiplatform") version "2.4.20-Beta1"
    }
}

rootProject.name = "diag-cross-absorb"

includeBuild("%%PARENT_BUILD%%")

include(":lib", ":app")
