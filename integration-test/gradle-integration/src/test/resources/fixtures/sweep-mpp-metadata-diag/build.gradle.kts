plugins {
    kotlin("multiplatform")
    id("org.wrongwrong.sealed-class-enumizer")
}

repositories {
    mavenLocal()
    mavenCentral()
}

// 2 ターゲット以上で commonMain の metadata コンパイルが立つ（platform コンパイルを走らせずに
// :compileCommonMainKotlinMetadata 単独で診断発火を観測する）
kotlin {
    jvm()
    js {
        nodejs()
    }
}
