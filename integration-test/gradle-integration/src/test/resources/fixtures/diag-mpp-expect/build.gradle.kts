plugins {
    kotlin("multiplatform")
    id("org.wrongwrong.sealed-class-enumizer")
}

repositories {
    // プラグインが自動追加する runtime-api の解決先（docs/テストケース管理.md local-repo 経路）
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm()
}
