// TestKit フィクスチャ: 旧バイナリ差し替え（docs/概要.md §7・docs/test/ケース06-ビルド動態.md BLD-40）。
// 追加方向 = v1（2 末端）でコンパイルした consumer を実行時のみ v2（3 末端）の jar と組み合わせる。
// 削除方向 = v2 でコンパイルした consumer2 を実行時のみ v1 の jar と組み合わせる
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "2.4.20-Beta1"
        id("org.wrongwrong.sealed-class-enumizer") version "1.0-SNAPSHOT"
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "binary-swap"

include(
    ":producer-v1",
    ":producer-v2",
    ":consumer",
    ":consumer2",
)

buildCache {
    local {
        directory = "%%BUILD_CACHE_DIR%%"
    }
}
