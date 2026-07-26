plugins {
    kotlin("multiplatform")
    id("org.wrongwrong.sealed-class-enumizer")
}

repositories {
    // プラグインが自動追加する runtime-api の解決先（docs/test/フィクスチャ構成.md §4 の local-repo 経路）
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm()
}
