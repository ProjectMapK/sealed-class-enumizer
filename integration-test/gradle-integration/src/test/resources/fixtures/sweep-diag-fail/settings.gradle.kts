// sweep-diag-fail フィクスチャ（docs/テストケース管理.md 残ケース掃討・G 軸 診断カタログの補完。
// 複数の独立階層を 1 モジュールへ収容し、1 回の buildAndFail で発火系の残ケースをまとめて検証する）
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

rootProject.name = "sweep-diag-fail"

includeBuild("%%PARENT_BUILD%%")
