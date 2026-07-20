// diag-nearmiss-general フィクスチャ（docs/テストケース管理.md「Gradle TestKit 方針」・G 軸 診断カタログ）
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

rootProject.name = "diag-nearmiss-general"

includeBuild("%%PARENT_BUILD%%")
