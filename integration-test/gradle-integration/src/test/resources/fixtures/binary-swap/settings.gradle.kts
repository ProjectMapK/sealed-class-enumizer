// TestKit フィクスチャ: 旧バイナリ差し替え（docs/概要.md §7・docs/テストケース管理.md TC-XM-023 系）。
// v1（2 末端）でコンパイルした consumer を、実行時のみ v2（3 末端）の jar と組み合わせる
pluginManagement {
    includeBuild("%%PARENT_BUILD%%")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "binary-swap"

includeBuild("%%PARENT_BUILD%%")

include(
    ":producer-v1",
    ":producer-v2",
    ":consumer",
)

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
